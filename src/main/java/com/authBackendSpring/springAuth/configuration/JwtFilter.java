package com.authBackendSpring.springAuth.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.authBackendSpring.springAuth.serviceLayer.UsersService.JwtService;
import com.authBackendSpring.springAuth.serviceLayer.UsersService.UserService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Once per request so, needs to go with this filter once before going to API calls
@Component
public class JwtFilter extends OncePerRequestFilter {

   public static String userEmail;

    @Autowired
    ApplicationContext context;

    private final JwtService service;

    private static final List<String> PERMITTED_ENDPOINTS = Arrays.asList(
        "/api/v1/users/register", "/api/v1/users/login","/api/v1/users/send","/api/v1/users/sendOtp",
    "/api/v1/users/verifyOtp",
    "/api/v1/users/resendOtp",
    "/api/v1/users/refreshToken",
    "/api/v1/users/resendForgotPassOtp",
    "/api/v1/users/sendForgotPassOtp"
    );
    
    public JwtFilter(JwtService service) {
        this.service = service;
    }

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        if (PERMITTED_ENDPOINTS.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": 401, \"message\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7);

        try {
            String extractedEmail = service.extractUserEmail(token);
            
            if (extractedEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = context.getBean(UserService.class).loadUserByUsername(extractedEmail);
                
                if (service.validateToken(token, userDetails)) {
                    userEmail=extractedEmail;
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": 401, \"message\": \"Invalid or expired token. Please refresh.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
