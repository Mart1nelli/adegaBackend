package com.adegadopaibackend.adegadopaibackend.security;

import com.adegadopaibackend.adegadopaibackend.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PREFIX = "/api/v1/auth/";
    private static final String WEBHOOK_PATH = "/api/v1/payments/webhook";

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WindowState> windows = new ConcurrentHashMap<>();

    @Value("${security.rate-limit.auth.max-requests:10}")
    private int authMaxRequests;

    @Value("${security.rate-limit.auth.window-ms:60000}")
    private long authWindowMs;

    @Value("${security.rate-limit.api.max-requests:120}")
    private int apiMaxRequests;

    @Value("${security.rate-limit.api.window-ms:60000}")
    private long apiWindowMs;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return "OPTIONS".equalsIgnoreCase(method)
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.contains(WEBHOOK_PATH);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean authPath = path.startsWith(AUTH_PREFIX);
        int maxRequests = authPath ? authMaxRequests : apiMaxRequests;
        long windowMs = authPath ? authWindowMs : apiWindowMs;
        String clientKey = resolveClientKey(request) + ":" + (authPath ? "auth" : "api");

        if (!tryAcquire(clientKey, maxRequests, windowMs)) {
            writeTooManyRequests(response, path, windowMs);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryAcquire(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        AtomicBoolean allowed = new AtomicBoolean(false);

        WindowState state = windows.compute(key, (currentKey, currentState) -> {
            if (currentState == null || now - currentState.windowStart >= windowMs) {
                allowed.set(true);
                return new WindowState(now, 1);
            }

            if (currentState.requestCount >= maxRequests) {
                allowed.set(false);
                return currentState;
            }

            currentState.requestCount++;
            allowed.set(true);
            return currentState;
        });

        cleanupExpiredEntries(now);
        return state != null && allowed.get() && now - state.windowStart < windowMs;
    }

    private void cleanupExpiredEntries(long now) {
        long maxWindow = Math.max(authWindowMs, apiWindowMs);
        windows.entrySet().removeIf(entry -> now - entry.getValue().windowStart > maxWindow);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response, String path, long windowMs) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(Math.max(1, windowMs / 1000)));

        ApiError body = ApiError.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .message("Rate limit exceeded")
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }

    private static final class WindowState {
        private final long windowStart;
        private int requestCount;

        private WindowState(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
