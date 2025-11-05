package com.CodeForge.CodeForge.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.Exception.ContestAlreadyRunningException;
import com.CodeForge.CodeForge.Exception.ContestNotFoundException;
import com.CodeForge.CodeForge.Exception.ParticipantNotFoundException;
import com.CodeForge.CodeForge.Exception.ProblemNotFoundException;
import com.CodeForge.CodeForge.Exception.UserNotFoundException;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.ContestProblem;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.ContestParticipantRepository;
import com.CodeForge.CodeForge.repository.ContestProblemRepository;
import com.CodeForge.CodeForge.repository.ContestRepository;
import com.CodeForge.CodeForge.repository.ProblemRepository;
import com.CodeForge.CodeForge.repository.UserRepository;

@Service
@Transactional
public class ContestService {

    @Autowired
    private ContestRepository contestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContestProblemRepository contestProblemRepository;
    
    @Autowired
    private ContestParticipantRepository contestParticipantRepository;

    @Autowired
    private ProblemRepository problemRepository;

    // 1. Contest Management
    
    public Contest createContest(Contest contest) {
        // Validate contest data
        if (contest.getEndTime().isBefore(contest.getStartTime())) {
            throw new IllegalArgumentException("Contest end time must be after start time");
        }
        
        // Set initial status based on current time
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime())) {
            contest.setStatus(Contest.Status.UPCOMING);
        } else if (now.isAfter(contest.getEndTime())) {
            contest.setStatus(Contest.Status.COMPLETED);
        } else {
            contest.setStatus(Contest.Status.RUNNING);
        }
        
        return contestRepository.save(contest);
    }
    
    public Contest updateContest(Long contestId, Contest updatedContest) {
        Contest existingContest = contestRepository.findById(contestId)
            .orElseThrow(() -> new ContestNotFoundException(contestId));
            
        // Check if contest has already started
        if (existingContest.getStatus() == Contest.Status.RUNNING) {
             throw new ContestAlreadyRunningException(contestId);
        }
        
        // Update fields
        existingContest.setTitle(updatedContest.getTitle());
        existingContest.setDescription(updatedContest.getDescription());
        existingContest.setStartTime(updatedContest.getStartTime());
        existingContest.setEndTime(updatedContest.getEndTime());
        
        return contestRepository.save(existingContest);
    }
    
    public void deleteContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
            .orElseThrow(() -> new ContestNotFoundException(contestId));
            
        // Check if contest has already started
        if (contest.getStatus() != Contest.Status.UPCOMING) {
            throw new IllegalStateException("Cannot delete a contest that has already started or ended");
        }
        
        contestRepository.delete(contest);
    }
    
    public Contest getContest(Long contestId) {
        return contestRepository.findById(contestId)
            .orElseThrow(() -> new ContestNotFoundException(contestId));
    }
    
    public List<Contest> listContests(Contest.Status status) {
        if (status != null) {
            return contestRepository.findByStatus(status);
        }
        return contestRepository.findAll();
    }
    
    public Contest cloneContest(Long contestId, String newTitle, LocalDateTime newStartTime) {
        Contest original = getContest(contestId);
        
        Contest clonedContest = new Contest();
        clonedContest.setTitle(newTitle);
        clonedContest.setDescription(original.getDescription() + " (Cloned)");
        clonedContest.setStartTime(newStartTime);
        
        // Calculate end time based on original duration
        long durationMinutes = java.time.Duration.between(
            original.getStartTime(), original.getEndTime()).toMinutes();
        clonedContest.setEndTime(newStartTime.plusMinutes(durationMinutes));
        
        clonedContest.setStatus(Contest.Status.UPCOMING);
        
        Contest savedContest = contestRepository.save(clonedContest);
        
        return contestRepository.save(savedContest);
    }

    // 2. Contest Registration
    public List<Contest> getAllContests()
    {
        return contestRepository.findAll();
    }
    
    public void registerUser(Long contestId, Long userId) {
        Contest contest = getContest(contestId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        // Check if contest is open for registration (allow UPCOMING and RUNNING)
        if (contest.getStatus() == Contest.Status.COMPLETED) {
            throw new IllegalStateException("Registration is closed for this contest");
        }
        
        // Check if user is already registered
        if (contestParticipantRepository.existsByContestAndUser(contest, user)) {
            throw new IllegalStateException("User is already registered for this contest");
        }
        
        ContestParticipant participant = new ContestParticipant();
        participant.setContest(contest);
        participant.setUser(user);
        
        contestParticipantRepository.save(participant);
    }
    
    public void unregisterUser(Long contestId, Long userId) {
        Contest contest = getContest(contestId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        // Check if contest has already started
        if (contest.getStatus() == Contest.Status.RUNNING) {
            throw new IllegalStateException("Cannot unregister from an active contest");
        }
        
        ContestParticipant participant = contestParticipantRepository.findByContestAndUser(contest, user)
            .orElseThrow(() -> new ParticipantNotFoundException(contestId, userId));
            
        contestParticipantRepository.delete(participant);
    }
    
    public List<ContestParticipant> getRegisteredUsers(Long contestId) {
        Contest contest = getContest(contestId);
        return contestParticipantRepository.findByContest(contest);
    }
    
    public boolean checkRegistration(Long contestId, Long userId) {
        Contest contest = getContest(contestId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        return contestParticipantRepository.existsByContestAndUser(contest, user);
    }

    public ContestParticipant getUserRankInContest(Long contestId, Long userId) {
        Contest contest = getContest(contestId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        Optional<ContestParticipant> participant = contestParticipantRepository.findByContestAndUser(contest, user);
        return participant.orElse(null);
    }

    // 3. Contest Problem Management
    
    public void addProblemToContest(Long contestId, Long problemId, Integer points) {
        Contest contest = getContest(contestId);
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new ProblemNotFoundException(problemId));
        
        // Allow adding problems to UPCOMING or RUNNING contests (for flexibility)
        if (contest.getStatus() == Contest.Status.COMPLETED) {
            throw new IllegalStateException("Cannot add problems to a contest that has already ended");
        }
        
        // Check if problem is already in contest
        if (contestProblemRepository.existsByContestAndProblem(contest, problem)) {
            throw new IllegalStateException("Problem is already in this contest");
        }
        
        // Create ContestProblem with points
        ContestProblem contestProblem = new ContestProblem();
        contestProblem.setContest(contest);
        contestProblem.setProblem(problem);
        contestProblem.setPoints(points != null ? points : 100); // Default 100 points
        
        contestProblemRepository.save(contestProblem);
    }
    
    public void removeProblem(Long contestId, Long problemId) {
        Contest contest = getContest(contestId);
        
        // Check if contest has already started
        if (contest.getStatus() != Contest.Status.UPCOMING) {
            throw new IllegalStateException("Cannot remove problems from a contest that has already started");
        }
        
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new ProblemNotFoundException(problemId));
        
        // Find the ContestProblem association
        ContestProblem contestProblem = contestProblemRepository.findByContestAndProblem(contest, problem)
            .orElseThrow(() -> new IllegalArgumentException("Problem does not belong to this contest"));
        
        // Delete the ContestProblem association (not the Problem itself)
        contestProblemRepository.delete(contestProblem);
    }
    
    public List<ContestProblem> getContestProblems(Long contestId) {
        Contest contest = getContest(contestId);
        return contestProblemRepository.findByContest(contest); 
    }

    // 4. Contest Timing & State Management
    
    public void startContest(Long contestId) {
        Contest contest = getContest(contestId);
        
        if (contest.getStatus() != Contest.Status.UPCOMING) {
            throw new IllegalStateException("Contest can only be started if it's upcoming");
        }
        
        contest.setStatus(Contest.Status.RUNNING);
        contest.setStartTime(LocalDateTime.now());
        
        contestRepository.save(contest);
    }
    
    public void endContest(Long contestId) {
        Contest contest = getContest(contestId);
        
        if (contest.getStatus() != Contest.Status.RUNNING) {
            throw new IllegalStateException("Contest can only be ended if it's active");
        }
        
        contest.setStatus(Contest.Status.COMPLETED);
        contest.setEndTime(LocalDateTime.now());
        contestRepository.save(contest);
    }
    
    public void extendContest(Long contestId, int minutesToAdd) {
        Contest contest = getContest(contestId);
        
        if (contest.getStatus() != Contest.Status.RUNNING) {
            throw new IllegalStateException("Can only extend an active contest");
        }
        
        if (minutesToAdd <= 0) {
            throw new IllegalArgumentException("Extension time must be positive");
        }
        
        contest.setEndTime(contest.getEndTime().plusMinutes(minutesToAdd));
        contestRepository.save(contest);
    }
    
    public Contest.Status getContestStatus(Long contestId) {
        Contest contest = getContest(contestId);
        return contest.getStatus();
    }
    
    public void scheduleContest(Contest contest) {
        if (contest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Contest start time must be in the future");
        }
        
        contest.setStatus(Contest.Status.UPCOMING);
        contestRepository.save(contest);
    }

    // 5. Leaderboard Services
    
    public Integer getUserRank(Long contestId, Long userId) {
        Contest contest = getContest(contestId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        if (contest.getStatus() == Contest.Status.UPCOMING) {
            throw new IllegalStateException("Rank is not available for upcoming contests");
        }
        
        // You'll need to implement getUserRank method
        // return leaderboardService.getUserRank(contestId, userId);
        return 0;
    }

    // 6. Contest Analytics
    
    public String exportContestData(Long contestId, ExportFormat format) {
        Contest contest = getContest(contestId);
        
        if (contest.getStatus() == Contest.Status.UPCOMING) {
            throw new IllegalStateException("Cannot export data for upcoming contests");
        }
        
        // Implementation depends on the export format
        switch (format) {
            case CSV:
                return exportContestDataAsCsv(contestId);
            case JSON:
                return exportContestDataAsJson(contestId);
            case EXCEL:
                return exportContestDataAsExcel(contestId);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }
    
    private String exportContestDataAsCsv(Long contestId) {
        return "CSV export data for contest: " + contestId;
    }
    
    private String exportContestDataAsJson(Long contestId) {
        return "JSON export data for contest: " + contestId;
    }
    
    private String exportContestDataAsExcel(Long contestId) {
        return "Excel export data for contest: " + contestId;
    }

    // 7. Contest Validation
    
    public boolean validateContestAccess(Long contestId, Long userId) {
        Contest contest = getContest(contestId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
            
        // Check if user is registered for the contest
        boolean isRegistered = checkRegistration(contestId, userId);
        
        // Check if contest is active or ended (users can view ended contests)
        boolean isAccessible = contest.getStatus() != Contest.Status.UPCOMING;
        
        return isRegistered && isAccessible;
    }

    public Contest getActiveContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
        if (!contest.getStatus().equals(Contest.Status.RUNNING)) {
            throw new IllegalStateException("Contest is not running");
        }
        return contest;
    }
    
    public boolean checkContestConstraints(Long contestId, Submission submission) {
        Contest contest = getContest(contestId);
        
        // Check if contest is active
        if (contest.getStatus() != Contest.Status.RUNNING) {
            return false;
        }
        
        // Check if submission is within contest time
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime()) || now.isAfter(contest.getEndTime())) {
            return false;
        }
        
        // Check if problem belongs to contest
        Problem problem = submission.getProblem();
        
        if(!contestProblemRepository.existsByContestAndProblem(contest, problem)) {
            return false;
        }
        
        return true;
    }

    // FIXED: Use Contest.Status.RUNNING instead of Status.ACTIVE
    public long getActiveContestsCount() {
        return contestRepository.countByStatus(Contest.Status.RUNNING);
    }
    
    public long getParticipantCount(Long contestId) {
        return contestParticipantRepository.countByContestId(contestId);
    }
}

// Move ExportFormat enum to its own file or make it a static inner class
enum ExportFormat {
    CSV, JSON, EXCEL
}