package com.CodeForge.CodeForge.service;

import com.CodeForge.CodeForge.model.*;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.repository.ProblemRepository;
import com.CodeForge.CodeForge.repository.ContestRepository;
import com.CodeForge.CodeForge.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jvnet.hk2.annotations.Service;

import com.CodeForge.CodeForge.repository.ContestProblemRepository;


@Service
@jakarta.transaction.Transactional

public class SubmissionService
{
    @Autowired
    private  SubmissionRepository submissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired 
    private ContestProblemRepository contestProblemRepository;

    @Autowired
    private ContestRepository contestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContestService contestService;


}




