package com.CodeForge.CodeForge.Controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.Exception.UserNotAuthorizedException;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.CodeForge.CodeForge.services.ContestService;




@RestController
@RequestMapping("/api/contests")
public class ContestController {

    @Autowired
    private ContestService contestService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping
    public ResponseEntity<List<Contest>> getAllContests() {
        List<Contest> contests = contestService.getAllContests();
        return ResponseEntity.ok(contests);  // 200 OK + JSON list
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contest> getContest(@PathVariable Long id) {
        Contest contest = contestService.getContest(id); // ✅ match service method
        if (contest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contest);  
    }

    @PostMapping
    public ResponseEntity<Contest> createContest(
            @RequestBody Contest contest,
            @AuthenticationPrincipal User user) { 

        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.PROBLEM_SETTER) {
            return ResponseEntity.status(403).build();  
        }

        contest.setCreatedBy(user);  
        Contest savedContest = contestService.createContest(contest);
        return ResponseEntity.status(201).body(savedContest);  
    }

   @PutMapping("/{id}")
public ResponseEntity<Contest> updateContest(
        @PathVariable Long id,
        @RequestBody Contest contest,
        @AuthenticationPrincipal User user) 
    {
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.PROBLEM_SETTER) {
            throw new UserNotAuthorizedException("update contest");
        }

        Contest updatedContest = contestService.updateContest(id, contest);
        return ResponseEntity.ok(updatedContest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        // Authorization check
        if (user.getRole() != User.Role.ADMIN) {
            throw new UserNotAuthorizedException("delete contest");
        }

        // Let the service handle deletion and throw ContestNotFoundException if needed
        contestService.deleteContest(id);

        return ResponseEntity.ok().build();  // 200 OK, no body
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinContest(
            @PathVariable Long id,
            Principal principal) {
        try {
            String username = principal.getName(); // logged-in username
            // Lookup your User from DB by username
            User dbUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

            contestService.registerUser(id, dbUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }


    @GetMapping("/{id}/participants")
public ResponseEntity<List<ContestParticipant>> getParticipants(
        @PathVariable Long id,
        java.security.Principal principal,
        org.springframework.security.core.Authentication authentication) {

    boolean isSpringAdmin = authentication != null && authentication.getAuthorities() != null &&
            authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    boolean isDbAdmin = false;
    if (principal != null) {
        String username = principal.getName();
        User dbUser = userRepository.findByUsername(username).orElse(null);
        isDbAdmin = dbUser != null && dbUser.getRole() == User.Role.ADMIN;
    }

    if (!isSpringAdmin && !isDbAdmin) {
        throw new UserNotAuthorizedException("view contest participants");
    }

    List<ContestParticipant> participants = contestService.getRegisteredUsers(id);

    return ResponseEntity.ok(participants);  // 200 OK
}

    @PostMapping("/{contestId}/problems/{problemId}")
    public ResponseEntity<Void> addProblemToContest(
            @PathVariable Long contestId,
            @PathVariable Long problemId,
            @RequestParam(name = "points", required = false) Integer points,
            java.security.Principal principal,
            org.springframework.security.core.Authentication authentication) {

        boolean isSpringAdmin = authentication != null && authentication.getAuthorities() != null &&
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isDbAdmin = false;
        if (principal != null) {
            String username = principal.getName();
            User dbUser = userRepository.findByUsername(username).orElse(null);
            isDbAdmin = dbUser != null && dbUser.getRole() == User.Role.ADMIN;
        }

        if (!isSpringAdmin && !isDbAdmin) {
            throw new UserNotAuthorizedException("add problem to contest");
        }

        contestService.addProblemToContest(contestId, problemId, points);
        return ResponseEntity.ok().build();
    }





}
