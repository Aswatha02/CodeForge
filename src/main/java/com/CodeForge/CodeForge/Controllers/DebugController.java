package com.CodeForge.CodeForge.Controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.util.JwtUtil;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/jwt")
    public ResponseEntity<?> debugJwt(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            response.put("token_present", true);
            response.put("token_length", token.length());
            response.put("token_prefix", token.substring(0, Math.min(20, token.length())) + "...");
            
            try {
                String username = jwtUtil.extractUsername(token);
                response.put("username", username);
                response.put("valid", true);
            } catch (Exception e) {
                response.put("valid", false);
                response.put("error", e.getMessage());
            }
        } else {
            response.put("token_present", false);
        }
        
        return ResponseEntity.ok(response);
    }
}