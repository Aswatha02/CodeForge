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
import com.CodeForge.CodeForge.dto.*;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.ContestProblem;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.services.ContestService;
import com.CodeForge.CodeForge.model.Submission;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;




@RestController
@RequestMapping("/api/contests")
public class ContestController {

    @Autowired
    private ContestService contestService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;


    @GetMapping
    public ResponseEntity<List<ContestResponse>> getAllContests(
            @RequestParam(required = false) String status,
            Principal principal) {
        
        Contest.Status contestStatus = null;
        if (status != null) {
            try {
                contestStatus = Contest.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<Contest> contests = contestService.listContests(contestStatus);
        Long currentUserId = getCurrentUserId(principal);
        
        List<ContestResponse> responses = contests.stream()
            .map(contest -> {
                ContestResponse response = new ContestResponse(contest);
                response.setParticipantCount(contestService.getParticipantCount(contest.getId()));
                response.setProblemCount(contestService.getContestProblems(contest.getId()).size());
                
                if (currentUserId != null) {
                    response.setIsRegistered(contestService.checkRegistration(contest.getId(), currentUserId));
                }
                
                // Calculate time remaining
                LocalDateTime now = LocalDateTime.now();
                if (contest.getStatus() == Contest.Status.UPCOMING) {
                    response.setTimeRemaining(Duration.between(now, contest.getStartTime()).getSeconds());
                } else if (contest.getStatus() == Contest.Status.RUNNING) {
                    response.setTimeRemaining(Duration.between(now, contest.getEndTime()).getSeconds());
                }
                
                return response;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestResponse> getContest(@PathVariable Long id) {
        Contest contest = contestService.getContest(id);
        if (contest == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Convert to DTO to avoid lazy loading serialization issues
        ContestResponse response = new ContestResponse(contest);
        response.setParticipantCount(contestService.getParticipantCount(id));
        response.setProblemCount(contestService.getContestProblems(id).size());
        
        return ResponseEntity.ok(response);  
    }

    @PostMapping
    public ResponseEntity<?> createContest(
            @RequestBody ContestCreateRequest request,
            Principal principal) { 

        try {
            User user = getCurrentUser(principal);
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.PROBLEM_SETTER) {
                return ResponseEntity.status(403).body(Map.of("message", "Insufficient permissions"));  
            }

            // Validate request
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Contest title is required"));
            }
            if (request.getStartTime() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Start time is required"));
            }
            if (request.getEndTime() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "End time is required"));
            }

            Contest contest = new Contest();
            contest.setTitle(request.getTitle());
            contest.setDescription(request.getDescription());
            contest.setStartTime(request.getStartTime());
            contest.setEndTime(request.getEndTime());
            contest.setDuration(request.getDuration());
            contest.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
            contest.setMaxParticipants(request.getMaxParticipants());
            contest.setCreatedBy(user);
            
            Contest savedContest = contestService.createContest(contest);
            
            // Add problems if provided
            if (request.getProblemIds() != null && !request.getProblemIds().isEmpty()) {
                for (Long problemId : request.getProblemIds()) {
                    try {
                        contestService.addProblemToContest(savedContest.getId(), problemId, 100);
                    } catch (Exception e) {
                        System.err.println("Error adding problem " + problemId + " to contest: " + e.getMessage());
                    }
                }
            }
            
            ContestResponse response = new ContestResponse(savedContest);
            response.setParticipantCount(0L);
            response.setProblemCount(request.getProblemIds() != null ? request.getProblemIds().size() : 0);
            
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error: " + e.getMessage()));
        }
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
    public ResponseEntity<?> joinContest(
            @PathVariable Long id,
            Principal principal) {
        try {
            String username = principal.getName(); // logged-in username
            // Lookup your User from DB by username
            User dbUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

            contestService.registerUser(id, dbUser.getId());
            return ResponseEntity.ok().body(Map.of("message", "Successfully joined the contest"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to join contest: " + e.getMessage()));
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
    
    // Get contest dashboard with all details
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ContestDashboardResponse> getContestDashboard(
            @PathVariable Long id,
            Principal principal) {
        
        Contest contest = contestService.getContest(id);
        User user = getCurrentUser(principal);
        
        // Check if user is registered
        if (!contestService.checkRegistration(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        ContestDashboardResponse dashboard = new ContestDashboardResponse();
        
        // Contest info
        ContestResponse contestResponse = new ContestResponse(contest);
        contestResponse.setParticipantCount(contestService.getParticipantCount(id));
        contestResponse.setIsRegistered(true);
        dashboard.setContest(contestResponse);
        
        // Problems
        List<ContestProblem> contestProblems = contestService.getContestProblems(id);
        List<ContestProblemResponse> problemResponses = contestProblems.stream()
            .map(cp -> {
                ContestProblemResponse pr = new ContestProblemResponse();
                pr.setId(cp.getProblem().getId());
                pr.setTitle(cp.getProblem().getTitle());
                pr.setDifficulty(cp.getProblem().getDifficulty().name());
                pr.setPoints(cp.getPoints());
                
                // Code editor fields - CRITICAL for code generation
                pr.setDescription(cp.getProblem().getDescription());
                pr.setFunctionName(cp.getProblem().getFunctionName());
                pr.setParameters(cp.getProblem().getParameters());
                pr.setReturnType(cp.getProblem().getReturnType());
                
                // Map categories
                List<CategoryDTO> categoryDTOs = cp.getProblem().getCategories().stream()
                    .map(CategoryDTO::new)
                    .collect(Collectors.toList());
                pr.setCategories(categoryDTOs);
                
                // Map code templates
                Map<String, ContestProblemResponse.CodeTemplate> templateMap = new HashMap<>();
                cp.getProblem().getCodeTemplates().forEach(ct -> {
                    templateMap.put(ct.getLanguage().name(), 
                        new ContestProblemResponse.CodeTemplate(ct.getVisibleCode(), ct.getHiddenCode()));
                });
                pr.setCodeTemplates(templateMap);
                
                // Map test cases
                List<ContestProblemResponse.TestCaseDTO> testCaseDTOs = cp.getProblem().getTestCases().stream()
                    .map(tc -> {
                        ContestProblemResponse.TestCaseDTO dto = new ContestProblemResponse.TestCaseDTO();
                        dto.setId(tc.getId());
                        dto.setInputData(tc.getInputData());
                        dto.setExpectedOutput(tc.getExpectedOutput());
                        dto.setIsSample(tc.getIsSample());
                        dto.setWeight(tc.getWeight());
                        return dto;
                    })
                    .collect(Collectors.toList());
                pr.setTestCases(testCaseDTOs);
                
                // Get submission stats
                List<Submission> allSubmissions = submissionRepository.findByProblemId(cp.getProblem().getId());
                pr.setTotalSubmissions((int) allSubmissions.stream()
                    .filter(s -> contestService.checkRegistration(id, s.getUser().getId()))
                    .count());
                pr.setAcceptedSubmissions((int) allSubmissions.stream()
                    .filter(s -> s.getStatus() == Submission.Status.ACCEPTED)
                    .filter(s -> contestService.checkRegistration(id, s.getUser().getId()))
                    .count());
                
                // User-specific stats
                List<Submission> userSubmissions = submissionRepository.findByUserIdAndProblemId(
                    user.getId(), cp.getProblem().getId());
                pr.setUserAttempts(userSubmissions.size());
                pr.setSolved(userSubmissions.stream()
                    .anyMatch(s -> s.getStatus() == Submission.Status.ACCEPTED));
                
                return pr;
            })
            .collect(Collectors.toList());
        dashboard.setProblems(problemResponses);
        
        // Leaderboard
        dashboard.setTopRanks(getLeaderboard(id).subList(0, Math.min(10, getLeaderboard(id).size())));
        dashboard.setUserRank(getUserLeaderboardEntry(id, user.getId()));
        
        // Stats
        ContestDashboardResponse.ContestStats stats = new ContestDashboardResponse.ContestStats();
        stats.setTotalParticipants(contestService.getParticipantCount(id));
        
        LocalDateTime now = LocalDateTime.now();
        if (contest.getStatus() == Contest.Status.RUNNING) {
            stats.setTimeElapsed(Duration.between(contest.getStartTime(), now).getSeconds());
            stats.setTimeRemaining(Duration.between(now, contest.getEndTime()).getSeconds());
        }
        
        dashboard.setStats(stats);
        
        return ResponseEntity.ok(dashboard);
    }
    
    // Get contest problems
    @GetMapping("/{id}/problems")
    public ResponseEntity<List<ContestProblemResponse>> getContestProblemsList(
            @PathVariable Long id,
            Principal principal) {
        
        User user = getCurrentUser(principal);
        
        // Check if user is registered
        if (!contestService.checkRegistration(id, user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        List<ContestProblem> contestProblems = contestService.getContestProblems(id);
        List<ContestProblemResponse> responses = contestProblems.stream()
            .map(cp -> {
                ContestProblemResponse pr = new ContestProblemResponse();
                pr.setId(cp.getProblem().getId());
                pr.setTitle(cp.getProblem().getTitle());
                pr.setDifficulty(cp.getProblem().getDifficulty().name());
                pr.setPoints(cp.getPoints());
                
                // Code editor fields - CRITICAL for code generation
                pr.setDescription(cp.getProblem().getDescription());
                pr.setFunctionName(cp.getProblem().getFunctionName());
                pr.setParameters(cp.getProblem().getParameters());
                pr.setReturnType(cp.getProblem().getReturnType());
                
                // Map categories
                List<CategoryDTO> categoryDTOs = cp.getProblem().getCategories().stream()
                    .map(CategoryDTO::new)
                    .collect(Collectors.toList());
                pr.setCategories(categoryDTOs);
                
                // Map code templates
                Map<String, ContestProblemResponse.CodeTemplate> templateMap = new HashMap<>();
                cp.getProblem().getCodeTemplates().forEach(ct -> {
                    templateMap.put(ct.getLanguage().name(), 
                        new ContestProblemResponse.CodeTemplate(ct.getVisibleCode(), ct.getHiddenCode()));
                });
                pr.setCodeTemplates(templateMap);
                
                // Map test cases
                List<ContestProblemResponse.TestCaseDTO> testCaseDTOs = cp.getProblem().getTestCases().stream()
                    .map(tc -> {
                        ContestProblemResponse.TestCaseDTO dto = new ContestProblemResponse.TestCaseDTO();
                        dto.setId(tc.getId());
                        dto.setInputData(tc.getInputData());
                        dto.setExpectedOutput(tc.getExpectedOutput());
                        dto.setIsSample(tc.getIsSample());
                        dto.setWeight(tc.getWeight());
                        return dto;
                    })
                    .collect(Collectors.toList());
                pr.setTestCases(testCaseDTOs);
                
                // User-specific stats
                List<Submission> userSubmissions = submissionRepository.findByUserIdAndProblemId(
                    user.getId(), cp.getProblem().getId());
                pr.setUserAttempts(userSubmissions.size());
                pr.setSolved(userSubmissions.stream()
                    .anyMatch(s -> s.getStatus() == Submission.Status.ACCEPTED));
                
                return pr;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    // Check registration status
    @GetMapping("/{id}/registration-status")
    public ResponseEntity<Map<String, Boolean>> checkRegistrationStatus(
            @PathVariable Long id,
            Principal principal) {
        
        Long userId = getCurrentUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        boolean isRegistered = contestService.checkRegistration(id, userId);
        return ResponseEntity.ok(Map.of("isRegistered", isRegistered));
    }
    
    // Leave contest
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveContest(
            @PathVariable Long id,
            Principal principal) {
        
        User user = getCurrentUser(principal);
        contestService.unregisterUser(id, user.getId());
        return ResponseEntity.ok().build();
    }
    
    // Start contest (admin only)
    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startContest(
            @PathVariable Long id,
            Principal principal) {
        
        User user = getCurrentUser(principal);
        if (user.getRole() != User.Role.ADMIN) {
            throw new UserNotAuthorizedException("start contest");
        }
        
        contestService.startContest(id);
        return ResponseEntity.ok().build();
    }
    
    // End contest (admin only)
    @PostMapping("/{id}/end")
    public ResponseEntity<Void> endContest(
            @PathVariable Long id,
            Principal principal) {
        
        User user = getCurrentUser(principal);
        if (user.getRole() != User.Role.ADMIN) {
            throw new UserNotAuthorizedException("end contest");
        }
        
        contestService.endContest(id);
        return ResponseEntity.ok().build();
    }
    
    // Helper methods
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName())
            .map(User::getId)
            .orElse(null);
    }
    
    private List<LeaderboardEntry> getLeaderboard(Long contestId) {
        List<ContestParticipant> participants = contestService.getRegisteredUsers(contestId);
        List<ContestProblem> problems = contestService.getContestProblems(contestId);
        
        List<LeaderboardEntry> leaderboard = participants.stream()
            .map(participant -> {
                User user = participant.getUser();
                int totalScore = 0;
                int problemsSolved = 0;
                long totalTime = 0;
                
                for (ContestProblem cp : problems) {
                    List<Submission> submissions = submissionRepository.findByUserIdAndProblemId(
                        user.getId(), cp.getProblem().getId());
                    
                    boolean solved = submissions.stream()
                        .anyMatch(s -> s.getStatus() == Submission.Status.ACCEPTED);
                    
                    if (solved) {
                        totalScore += cp.getPoints();
                        problemsSolved++;
                    }
                }
                
                return new LeaderboardEntry(
                    0, // rank will be set later
                    user.getId(),
                    user.getUsername(),
                    totalScore,
                    problemsSolved,
                    totalTime,
                    0
                );
            })
            .sorted(Comparator.comparing(LeaderboardEntry::getScore).reversed()
                .thenComparing(LeaderboardEntry::getProblemsSolved).reversed())
            .collect(Collectors.toList());
        
        // Set ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }
        
        return leaderboard;
    }
    
    private LeaderboardEntry getUserLeaderboardEntry(Long contestId, Long userId) {
        List<LeaderboardEntry> leaderboard = getLeaderboard(contestId);
        return leaderboard.stream()
            .filter(entry -> entry.getUserId().equals(userId))
            .findFirst()
            .orElse(null);
    }
}
