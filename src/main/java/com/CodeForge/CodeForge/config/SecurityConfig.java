package com.CodeForge.CodeForge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // disable CSRF for testing with Postman/cURL
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/contests/**").authenticated() // secure endpoints
                .anyRequest().permitAll()
            )
            .httpBasic(); // enables basic auth

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}password") // {noop} means no encoding, for testing
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
