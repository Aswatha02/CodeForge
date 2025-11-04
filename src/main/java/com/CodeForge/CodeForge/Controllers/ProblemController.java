package com.CodeForge.CodeForge.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.CodeForge.CodeForge.dto.ProblemRequest;
import com.CodeForge.CodeForge.dto.ProblemResponseDTO;
import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.services.ProblemService;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemResponseDTO>> getAllProblems() {
        try {
            List<Problem> problems = problemService.getAllProblems();
            List<ProblemResponseDTO> response = problems.stream()
                    .map(ProblemResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponseDTO> getProblemById(@PathVariable Long id) {
        try {
            Optional<Problem> problemOpt = problemService.getProblemById(id);
            
            if (problemOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Problem problem = problemOpt.get();
            ProblemResponseDTO response = new ProblemResponseDTO(problem);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProblemResponseDTO> getProblemBySlug(@PathVariable String slug) {
        try {
            Optional<Problem> problemOpt = problemService.getProblemBySlug(slug);
            
            if (problemOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(new ProblemResponseDTO(problemOpt.get()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> createProblem(
            @RequestBody Problem problem,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not authenticated"));
        }
        
        if (!User.Role.ADMIN.equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access required"));
        }
        
        try {
            Problem createdProblem = problemService.createProblem(problem, user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ProblemResponseDTO(createdProblem));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateProblem(
            @PathVariable Long id,
            @RequestBody ProblemRequest problemRequest,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not authenticated"));
        }

        if (!User.Role.ADMIN.equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access required"));
        }

        try {
            Problem updatedProblem = problemService.updateProblem(id, problemRequest);
            return ResponseEntity.ok(new ProblemResponseDTO(updatedProblem));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteProblem(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not authenticated"));
        }
        
        if (!User.Role.ADMIN.equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access required"));
        }
        
        try {
            problemService.deleteProblem(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Problem deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProblemResponseDTO>> searchProblems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long categoryId) {
        
        try {
            List<Problem> problems = problemService.searchProblems(query, difficulty, categoryId);
            List<ProblemResponseDTO> response = problems.stream()
                    .map(ProblemResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProblemResponseDTO>> getProblemsByCategory(@PathVariable Long categoryId) {
        try {
            List<Problem> problems = problemService.getProblemsByCategory(categoryId);
            List<ProblemResponseDTO> response = problems.stream()
                    .map(ProblemResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<ProblemResponseDTO>> getProblemsByDifficulty(@PathVariable String difficulty) {
        try {
            Problem.Difficulty diff = Problem.Difficulty.valueOf(difficulty.toUpperCase());
            List<Problem> problems = problemService.getProblemsByDifficulty(diff);
            List<ProblemResponseDTO> response = problems.stream()
                    .map(ProblemResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/debug/{id}/test-cases")
public ResponseEntity<?> debugProblemTestCases(@PathVariable Long id) {
    try {
        Optional<Problem> problemOpt = problemService.getProblemById(id);
        if (problemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Problem problem = problemOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("problemId", problem.getId());
        response.put("problemTitle", problem.getTitle());
        response.put("totalTestCases", problem.getTestCases().size());
        response.put("sampleTestCases", problem.getTestCases().stream()
                .filter(TestCase::getIsSample)
                .count());
        
        List<Map<String, Object>> testCasesInfo = problem.getTestCases().stream()
                .map(tc -> {
                    Map<String, Object> tcInfo = new HashMap<>();
                    tcInfo.put("id", tc.getId());
                    tcInfo.put("inputData", tc.getInputData());
                    tcInfo.put("expectedOutput", tc.getExpectedOutput());
                    tcInfo.put("isSample", tc.getIsSample());
                    tcInfo.put("explanation", tc.getExplanation());
                    return tcInfo;
                })
                .collect(Collectors.toList());
        
        response.put("testCases", testCasesInfo);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}

    @GetMapping("/{problemId}/templates")
    public ResponseEntity<List<CodeTemplate>> getCodeTemplates(@PathVariable Long problemId) {
        try {
            List<CodeTemplate> templates = problemService.getCodeTemplates(problemId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
