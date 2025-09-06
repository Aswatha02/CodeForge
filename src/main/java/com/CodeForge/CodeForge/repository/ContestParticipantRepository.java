package com.codeforge.codeforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Contest;
import com.codeforge.codeforge.model.ContestParticipant;
import com.codeforge.codeforge.model.User;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Long> {

    List<ContestParticipant> findByContest(Contest contest);

    List<ContestParticipant> findByUser(User user);

    Optional<ContestParticipant> findByContestAndUser(Contest contest, User user);

    List<ContestParticipant> findByContestOrderByScoreDesc(Contest contest);
}
