package com.adegadopaibackend.adegadopaibackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        filter = new RateLimitFilter(objectMapper);
        ReflectionTestUtils.setField(filter, "authMaxRequests", 1);
        ReflectionTestUtils.setField(filter, "authWindowMs", 60_000L);
        ReflectionTestUtils.setField(filter, "apiMaxRequests", 2);
        ReflectionTestUtils.setField(filter, "apiWindowMs", 60_000L);
    }

    @Test
    void shouldBlockAfterConfiguredApiLimit() throws Exception {
        assertAllowed("/api/v1/products");
        assertAllowed("/api/v1/products");

        MockHttpServletRequest request = request("/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
    }

    @Test
    void shouldBypassWebhookEndpoint() throws Exception {
        MockHttpServletRequest request = request("/api/v1/payments/webhook");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void assertAllowed(String path) throws Exception {
        MockHttpServletRequest request = request(path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
