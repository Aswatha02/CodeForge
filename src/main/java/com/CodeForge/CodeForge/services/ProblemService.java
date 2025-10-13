package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.ProblemRepository;
import com.CodeForge.CodeForge.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public ProblemService(ProblemRepository problemRepository, UserRepository userRepository) {
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    public Problem createProblem(Problem problem, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        problem.setCreatedBy(creator);
        return problemRepository.save(problem);
    }

    public Problem getProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Page<Problem> getProblems(Pageable pageable) {
        return problemRepository.findAll(pageable);
    }
}
