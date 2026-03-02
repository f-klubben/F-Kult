package dk.fklub.fkult.presentation.controllers;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements Filter {

    // Map to store request counts per IP address
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // Create a unique key for IP + endpoint
        String key = httpReq.getRemoteAddr() + ":" + httpReq.getRequestURI();

        // Maximum requests allowed per minute
        int limit = switch (httpReq.getRequestURI()) {
            case "/api/auth/username" -> 10; // 10 req/min
            case "/api/movies/batchById" -> 100;
            case "/api/movies/batchByTconst" -> 100;
            case "/api/movies/search" -> 1000;
            case "/api/movies/count" -> 1000;
            case "/api/movies/poster" -> 1000;
            case "/api/movies/preview" -> 1000;
            case "/api/movies/poster/id" -> 1000;
            case "/api/themes" -> 100;
            case "/api/themes/New" -> 100;
            case "/api/themes/Old" -> 100;
            case "/api/themes/User" -> 100;
            case "/api/sound-sample" -> 50;
            case "/api/sound-sample/upload" -> 50;
            case "/api/sound-sample/delete" -> 50;
            case "/api/sound-sample/get-all" -> 50;
            case "/api/sound-sample/download" -> 50;
            case "/api/user/admin" -> 100;
            case "/api/user/admin/ban_user" -> 100;
            case "/api/user/admin/unban_user" -> 100;
            case "/api/user/id" -> 100;
            case "/api/user/full_name" -> 100;
            default -> 1000; // All other endpoints
        };

        // Initialize request count for the client IP address, and then increment it
        requestCounts.putIfAbsent(key, new AtomicInteger(0));
        int count = requestCounts.get(key).incrementAndGet();

        // Check if the request limit has been exceeded
        if (count > limit) {
            httpRes.resetBuffer();
            httpRes.setStatus(429);
            httpRes.setHeader("Access-Control-Allow-Origin", "*");
            httpRes.getWriter().write("Too many requests for: " + httpReq.getRequestURI());

            // Flush writer so the client get the respons
            httpRes.getWriter().flush();
            return;
        }

        // Allow the request to proceed
        chain.doFilter(request, response);
    }

    // Reset request counts, every minute
    @Scheduled(fixedRate = 60000)
    public void resetCounts() {
        requestCounts.clear();
    }

    // Servere start, initialized rate limiter
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("RateLimitingFilter initialized");
    }

    // Server close, clear history 
    @Override
    public void destroy() {
        requestCounts.clear();
        System.out.println("RateLimitingFilter destroyed");
    }
}