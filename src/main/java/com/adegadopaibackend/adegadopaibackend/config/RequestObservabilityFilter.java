package com.adegadopaibackend.adegadopaibackend.config;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestObservabilityFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final MeterRegistry meterRegistry;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        long startedAt = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationNanos = System.nanoTime() - startedAt;
            int status = response.getStatus();
            String method = request.getMethod();
            String path = request.getRequestURI();

            meterRegistry.counter("http.server.requests.total", "method", method, "status", String.valueOf(status)).increment();
            meterRegistry.timer("http.server.requests.duration", "method", method, "status", String.valueOf(status))
                    .record(durationNanos, TimeUnit.NANOSECONDS);
            response.setHeader(REQUEST_ID_HEADER, requestId);

            if (status >= 500) {
                log.error("HTTP request failed: method={}, path={}, status={}, durationMs={}, requestId={}",
                        method, path, status, durationNanos / 1_000_000, requestId);
            } else if (status >= 400) {
                log.warn("HTTP request rejected: method={}, path={}, status={}, durationMs={}, requestId={}",
                        method, path, status, durationNanos / 1_000_000, requestId);
            } else {
                log.debug("HTTP request completed: method={}, path={}, status={}, durationMs={}, requestId={}",
                        method, path, status, durationNanos / 1_000_000, requestId);
            }
        }
    }
}
