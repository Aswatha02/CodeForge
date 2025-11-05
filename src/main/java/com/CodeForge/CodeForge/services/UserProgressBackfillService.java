package com.CodeForge.CodeForge.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import com.CodeForge.CodeForge.repository.UserRepository;

@Service
public class UserProgressBackfillService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    /**
     * Backfill UserProgress data from existing submissions
     * This is a one-time migration to populate the user_progress table
     */
    @Transactional
    public Map<String, Object> backfillUserProgress() {
        System.out.println("=== Starting UserProgress Backfill ===");
        
        List<User> allUsers = userRepository.findAll();
        int usersProcessed = 0;
        int usersUpdated = 0;
        int totalSubmissionsProcessed = 0;

        for (User user : allUsers) {
            try {
                // Get or create UserProgress for this user
                UserProgress progress = userProgressRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            UserProgress newProgress = new UserProgress(user);
                            return userProgressRepository.save(newProgress);
                        });

                // Reset counts to recalculate from scratch
                progress.setSolvedCount(0);
                progress.setSolvedEasyCount(0);
                progress.setSolvedMediumCount(0);
                progress.setSolvedHardCount(0);
                progress.setTotalSubmissions(0);
                progress.setAcceptedSubmissions(0);
                progress.getSolvedProblems().clear();

                // Get all submissions for this user
                List<Submission> userSubmissions = submissionRepository.findByUser(user);
                totalSubmissionsProcessed += userSubmissions.size();

                // Track unique solved problems
                Map<Long, Problem> solvedProblemsMap = new HashMap<>();

                for (Submission submission : userSubmissions) {
                    // Update total submissions
                    progress.setTotalSubmissions(progress.getTotalSubmissions() + 1);

                    // If accepted, update accepted count and track problem
                    if (submission.getStatus() == Submission.Status.ACCEPTED) {
                        progress.setAcceptedSubmissions(progress.getAcceptedSubmissions() + 1);
                        
                        Problem problem = submission.getProblem();
                        // Only count each problem once
                        if (!solvedProblemsMap.containsKey(problem.getId())) {
                            solvedProblemsMap.put(problem.getId(), problem);
                            progress.addSolvedProblem(problem);
                        }
                    }

                    // Update last activity date
                    if (progress.getLastActivityDate() == null || 
                        submission.getSubmittedAt().isAfter(progress.getLastActivityDate())) {
                        progress.setLastActivityDate(submission.getSubmittedAt());
                    }
                }

                // Save updated progress
                userProgressRepository.save(progress);
                usersProcessed++;
                
                if (progress.getTotalSubmissions() > 0) {
                    usersUpdated++;
                    System.out.println("✅ Updated user: " + user.getUsername() + 
                                     " - Solved: " + progress.getSolvedCount() + 
                                     " - Total Submissions: " + progress.getTotalSubmissions());
                }

            } catch (Exception e) {
                System.err.println("❌ Error processing user " + user.getUsername() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("=== Backfill Complete ===");
        System.out.println("Users processed: " + usersProcessed);
        System.out.println("Users with submissions: " + usersUpdated);
        System.out.println("Total submissions processed: " + totalSubmissionsProcessed);

        // Return summary
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("usersProcessed", usersProcessed);
        result.put("usersUpdated", usersUpdated);
        result.put("totalSubmissions", totalSubmissionsProcessed);
        result.put("message", "UserProgress backfill completed successfully");

        return result;
    }

    /**
     * Get backfill status/statistics
     */
    public Map<String, Object> getBackfillStatus() {
        long totalUsers = userRepository.count();
        long usersWithProgress = userProgressRepository.count();
        long totalSubmissions = submissionRepository.count();

        Map<String, Object> status = new HashMap<>();
        status.put("totalUsers", totalUsers);
        status.put("usersWithProgress", usersWithProgress);
        status.put("totalSubmissions", totalSubmissions);
        status.put("needsBackfill", totalUsers != usersWithProgress);

        return status;
    }
}
