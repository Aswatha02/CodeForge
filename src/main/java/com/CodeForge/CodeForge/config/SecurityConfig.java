package com.CodeForge.CodeForge.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.CodeForge.CodeForge.filter.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Add this for method-level security
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Debug endpoints (temporary - remove in production)
                .requestMatchers("/api/debug/**").permitAll()
                
                // Admin endpoints require ADMIN role
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Problem setter endpoints
                .requestMatchers("/api/problems/create").hasAnyAuthority("ROLE_ADMIN", "ROLE_PROBLEM_SETTER")
                .requestMatchers("/api/problems/*/edit").hasAnyAuthority("ROLE_ADMIN", "ROLE_PROBLEM_SETTER")
                
                // Authenticated endpoints
                .requestMatchers("/api/submissions/**").authenticated()
                .requestMatchers("/api/problems/*/submit").authenticated()
                .requestMatchers("/api/users/*/progress").authenticated()
                .requestMatchers("/api/users/profile").authenticated()
                
                // Public endpoints - authentication
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                
                // Public endpoints - problem browsing
                .requestMatchers("/api/problems").permitAll()
                .requestMatchers("/api/problems/*").permitAll()
                .requestMatchers("/api/problems/*/templates").permitAll()
                .requestMatchers("/api/problems/search/**").permitAll()
                .requestMatchers("/api/problems/category/**").permitAll()
                
                // Public endpoints - categories
                .requestMatchers("/api/categories").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                
                // WebSocket and static resources
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // Default - require authentication for everything else
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5174"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition")); // Add exposed headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}