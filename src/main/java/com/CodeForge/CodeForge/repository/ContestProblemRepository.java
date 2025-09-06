package com.codeforge.codeforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Contest;
import com.codeforge.codeforge.model.ContestProblem;
import com.codeforge.codeforge.model.Problem;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {

    List<ContestProblem> findByContest(Contest contest);

    Optional<ContestProblem> findByContestAndProblem(Contest contest, Problem problem);

    List<ContestProblem> findByProblem(Problem problem);
}
