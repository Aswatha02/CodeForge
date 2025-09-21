package com.CodeForge.CodeForge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestProblem;
import com.CodeForge.CodeForge.model.Problem;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {

    List<ContestProblem> findByContest(Contest contest);

    Optional<ContestProblem> findByContestAndProblem(Contest contest, Problem problem);

    List<ContestProblem> findByProblem(Problem problem);

     boolean existsByContestAndProblem(Contest contest, Problem problem);

    // Remove the Problem-related methods since this repository only handles ContestProblem
    // If you need to work with Problem entities, create a separate ProblemRepository
}