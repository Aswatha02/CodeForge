package com.CodeForge.CodeForge.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserId(Long userId);
    List<Submission> findByProblemId(Long problemId);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.problem.id = :problemId ORDER BY s.submittedAt DESC")
    List<Submission> findByUserIdAndProblemId(@Param("userId") Long userId, @Param("problemId") Long problemId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    Long countAcceptedSubmissionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem WHERE s.user.id = :userId ORDER BY s.submittedAt DESC")
    List<Submission> findByUserIdWithProblem(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.status = 'ACCEPTED'")
    Long countTotalAcceptedSubmissions();

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Submission s WHERE s.status = 'ACCEPTED'")
    Long countActiveUsers();

    @Query("SELECT s FROM Submission s WHERE s.user = :user AND s.problem = :problem ORDER BY s.submittedAt DESC")
    List<Submission> findByUserAndProblem(@Param("user") User user, @Param("problem") Problem problem);
    List<Submission> findByUser(User user);

    @Modifying
    @Query("DELETE FROM Submission s WHERE s.problem.id = :problemId")
    void deleteByProblemId(@Param("problemId") Long problemId);

    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Submission s WHERE s.submittedAt >= :since")
    Long countDistinctUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.problem.id = :problemId AND s.status = 'ACCEPTED'")
    Long countAcceptedSubmissionsByProblemId(@Param("problemId") Long problemId);

    List<Submission> findTop10ByOrderBySubmittedAtDesc();

    @Query("SELECT s FROM Submission s WHERE s.user = :user ORDER BY s.submittedAt DESC")
    List<Submission> findByUserOrderBySubmittedAtDesc(@Param("user") User user);
}
