package com.ftle.tracker.interceptor;

import com.ftle.tracker.annotations.AdminOnly;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        if (method.hasMethodAnnotation(AdminOnly.class)) {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("""
                    { "error": "Admin access only" }
                """);
                return false;
            }
        }

        return true;
    }
}
