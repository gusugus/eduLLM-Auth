package config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        if (path.equals("/api/auth/login") || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")) {
            String ip = request.getRemoteAddr();
            long now = System.currentTimeMillis();

            long[] window = requestCounts.compute(ip, (key, existing) -> {
                if (existing == null || now - existing[0] > WINDOW_MILLIS) {
                    return new long[]{now, 1};
                }
                existing[1]++;
                return existing;
            });

            if (window[1] > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Demasiadas solicitudes. Intenta nuevamente en 1 minuto.\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
