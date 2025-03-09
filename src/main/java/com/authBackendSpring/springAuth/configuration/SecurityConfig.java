package com.authBackendSpring.springAuth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;


    public SecurityConfig(UserDetailsService service, JwtFilter jwtFilter) {
        this.userDetailsService = service;
        this.jwtFilter = jwtFilter;
       
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API (adjust if needed)
            .csrf(csrf -> csrf.disable())
            
            // Set session management to stateless (useful for REST APIs)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure endpoint security
            .authorizeHttpRequests(authorize -> authorize
                // Allow /register and /login endpoints without authentication
                .requestMatchers("/api/v1/users/register", "/api/v1/users/login", "/api/v1/users/send", "/api/v1/users/sendOtp",
                "/api/v1/users/verifyOtp", "/api/v1/users/resendOtp","/api/v1/users/resendForgotPassOtp", "/api/v1/users/sendForgotPassOtp").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Disable the default form login so you don't get redirected to a login page
            .formLogin(form -> form.disable())
            
            // Enable HTTP Basic authentication so you can test using Postman
            .httpBasic(org.springframework.security.config.Customizer.withDefaults())
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Define AuthenticationManager manually as we are setting up custom AuthProvider
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // bcrypt enabler, custom auth provider -> need to use custom auth manager
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // default auth provider for username and password is dao auth provider
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        // this is the service that will be used to get user from db like name, email, pass
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }
}
