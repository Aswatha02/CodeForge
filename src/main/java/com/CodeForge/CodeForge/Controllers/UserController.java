package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.services.UserService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public User login(@RequestParam String usernameOrEmail,
                      @RequestParam String password) {
        return userService.login(usernameOrEmail, password);
    }

    @GetMapping("/{id}/progress")
    public List<UserProgress> getProgress(@PathVariable Long id) {
        return userService.getUserProgress(id);
    }
}
