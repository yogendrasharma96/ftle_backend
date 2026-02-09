package com.ftle.tracker.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || request.getRequestURI().startsWith("/api/public")
                || request.getRequestURI().startsWith("/api/auth")
                || request.getRequestURI().startsWith("/api/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "Missing or invalid Authorization header");
            return;
        }

        try {
            String token = authHeader.substring(7);
            FirebaseToken decodedToken =
                    FirebaseAuth.getInstance().verifyIdToken(token);

            request.setAttribute("uid", decodedToken.getUid());
            request.setAttribute("email", decodedToken.getEmail());
            request.setAttribute(
                    "role",
                    decodedToken.getClaims().getOrDefault("role", "USER")
            );

            filterChain.doFilter(request, response);

        } catch (FirebaseAuthException e) {
            writeError(response, 401, "Invalid or expired token");
        }
    }

    private void writeError(HttpServletResponse response, int status, String msg)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("""
            {
              "status": %d,
              "error": "%s"
            }
        """.formatted(status, msg));
    }
}