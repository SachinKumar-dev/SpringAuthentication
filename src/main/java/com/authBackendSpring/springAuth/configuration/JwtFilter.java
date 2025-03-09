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
    public static String userEmail = "";

    @Autowired
    ApplicationContext context;

    private final JwtService service;

    // List of endpoints that are permitted without authentication
    private static final List<String> PERMITTED_ENDPOINTS = Arrays.asList("/api/v1/users/register", "/api/v1/users/login","/api/v1/users/send","/api/v1/users/sendOtp",
                "/api/v1/users/verifyOtp",
                "/api/v1/users/resendOtp",
                "/api/v1/users/resendForgotPassOtp",
                "/api/v1/users/sendForgotPassOtp"
                );

    public JwtFilter(JwtService service) {
        this.service = service;
    }

    @Override
    @SuppressWarnings("null")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Check if the request URI is in the list of permitted endpoints
        String requestURI = request.getRequestURI();
        if (PERMITTED_ENDPOINTS.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Frontend we will get like: Bearer tokenmklkikalappo -> so remove prefix first
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String extractedEmail = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
            extractedEmail = service.extractUserEmail(token);
            // Setting it to static variable
            userEmail = service.extractUserEmail(token);
            } catch (JwtException e) {
            // Handle invalid token format
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": 401, \"message\": \"Invalid token format\"}");
            return; 
            }

            if (extractedEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String refreshTokenFromDb = service.getRefreshToken(extractedEmail);
            if (refreshTokenFromDb == null) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\": 401, \"message\": \"User not found\"}");
                return; 
            }
            // Extract user details from DB
            UserDetails userDetails = context.getBean(UserService.class).loadUserByUsername(extractedEmail);

            // If token is valid, authenticate the user
            if (service.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Bind authentication details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } 
            else if (service.isTokenExpired(token) && !service.isTokenExpired(refreshTokenFromDb)){
                // If access token is expired, send a new token response
                String newAccessToken = service.generateAccessToken(extractedEmail);
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\": 402, \"message\": \"Token expired\", \"newAccessToken\": \"" + newAccessToken + "\"}");
                return; // Stop further execution
            } 
            else if(service.isTokenExpired(refreshTokenFromDb)){
                // If both access and refresh tokens are expired, force logout
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"status\": 403, \"message\": \"Session expired, please log in again\"}");
                return; // Stop further execution
            }
            else {
                // If token is invalid
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\": 401, \"message\": \"Invalid token\"}");
                return; // Stop further execution
            }
            }
        } else {
            // If Authorization header is missing or does not start with Bearer
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": 401, \"message\": \"Missing or invalid Authorization header\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
