package org.springboot.backend.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple per-client limit on the endpoints that cost an AI call: at most N requests per
 * minute per IP address. Enough for one clerk's counter; stops runaway scripts.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int perMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${bvs.rate-limit.per-minute:30}") int perMinute) {
        this.perMinute = perMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !"POST".equals(request.getMethod()) || !(path.equals("/api/ask") || path.equals("/api/search"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long minute = System.currentTimeMillis() / 60_000;
        Window window = windows.compute(request.getRemoteAddr(),
                (ip, w) -> w == null || w.minute != minute ? new Window(minute, 1) : new Window(minute, w.count + 1));
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> e.getValue().minute != minute);
        }
        if (window.count > perMinute) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"RATE_LIMITED\",\"message\":\"Too many questions. Please wait a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long minute, int count) {
    }
}
