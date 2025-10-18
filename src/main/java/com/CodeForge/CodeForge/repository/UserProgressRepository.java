package com.CodeForge.CodeForge.repository;

import com.CodeForge.CodeForge.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    // FIXED: Check if user has solved a specific problem using the solvedProblems relationship
    @Query("SELECT up FROM UserProgress up JOIN up.solvedProblems sp WHERE up.user.id = :userId AND sp.id = :problemId")
    Optional<UserProgress> findByUserIdAndProblemId(@Param("userId") Long userId, @Param("problemId") Long problemId);

    // FIXED: Find by user ID using the user relationship
    Optional<UserProgress> findByUser_Id(Long userId);

    // This should work as is since Status is a field in UserProgress
    List<UserProgress> findByStatus(UserProgress.Status status);

    // FIXED: Find user progresses where a specific problem is in solvedProblems
    @Query("SELECT up FROM UserProgress up JOIN up.solvedProblems sp WHERE sp.id = :problemId")
    List<UserProgress> findBySolvedProblems_Id(@Param("problemId") Long problemId);

    // FIXED: Delete user progresses where a specific problem is in solvedProblems
    @Modifying
    @Query("DELETE FROM UserProgress up WHERE up.id IN (SELECT up2.id FROM UserProgress up2 JOIN up2.solvedProblems sp WHERE sp.id = :problemId)")
    void deleteByProblemId(@Param("problemId") Long problemId);

    // FIXED: Count solved problems for a user (using solvedCount field or checking solvedProblems size)
    @Query("SELECT up.solvedCount FROM UserProgress up WHERE up.user.id = :userId")
    Long countSolvedByUserId(@Param("userId") Long userId);

    // FIXED: Count attempted problems (total submissions minus accepted)
    @Query("SELECT (up.totalSubmissions - up.acceptedSubmissions) FROM UserProgress up WHERE up.user.id = :userId")
    Long countAttemptedByUserId(@Param("userId") Long userId);

    // FIXED: Count users who solved a specific problem
    @Query("SELECT COUNT(DISTINCT up) FROM UserProgress up JOIN up.solvedProblems sp WHERE sp.id = :problemId")
    Long countUsersWhoSolvedProblem(@Param("problemId") Long problemId);

    // ADDITIONAL HELPER METHODS you might find useful:
    
    // Check if user has solved a problem (boolean version)
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END " +
           "FROM UserProgress up JOIN up.solvedProblems sp " +
           "WHERE up.user.id = :userId AND sp.id = :problemId")
    boolean hasUserSolvedProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);

    // Find user progress by user ID (alternative to findByUser_Id)
    default Optional<UserProgress> findByUserId(Long userId) {
        return findByUser_Id(userId);
    }
}