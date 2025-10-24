package com.CodeForge.CodeForge.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.CodeForge.CodeForge.services.CustomUserDetailsService;
import com.CodeForge.CodeForge.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            logger.debug("=== JWT FILTER DEBUG ===");
            logger.debug("Request URI: {}", request.getRequestURI());
            logger.debug("JWT Present: {}", jwt != null);
            
            if (StringUtils.hasText(jwt)) {
                logger.debug("JWT Token: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
                
                if (jwtUtil.validateToken(jwt)) {
                    String username = jwtUtil.extractUsername(jwt);
                    logger.debug("Username from JWT: {}", username);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("User Authorities: {}", userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set successfully for user: {}", username);
                } else {
                    logger.warn("JWT validation failed for request: {}", request.getRequestURI());
                    // Don't set authentication - let it remain null
                }
            } else {
                logger.debug("No JWT token found in request");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context for URI: {}", 
                        request.getRequestURI(), ex);
            // Clear the security context in case of any error
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Don't apply JWT filter to login, register, or public endpoints
        return path.startsWith("/api/auth/") || 
               path.equals("/") || 
               path.startsWith("/error") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars/");
    }
}