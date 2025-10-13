package com.CodeForge.CodeForge.Exception;

public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException(Long contestId, Long userId) {
        super("Participant not found for contest: " + contestId + " and user: " + userId);
    }
}
