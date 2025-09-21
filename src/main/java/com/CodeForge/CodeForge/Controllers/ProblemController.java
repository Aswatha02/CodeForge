package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.services.ProblemService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;
    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping
    public Problem createProblem(@RequestBody Problem problem,
                                 @RequestParam Long creatorId) {
        return problemService.createProblem(problem, creatorId);
    }

    @GetMapping("/{id}")
    public Problem getProblem(@PathVariable Long id) {
        return problemService.getProblem(id);
    }

    @GetMapping
    public List<Problem> listProblems() {
        return problemService.getAllProblems();
    }

    @GetMapping("/paged")
    public Page<Problem> listProblemsPaged(Pageable pageable) {
        return problemService.getProblems(pageable);
    }
}
