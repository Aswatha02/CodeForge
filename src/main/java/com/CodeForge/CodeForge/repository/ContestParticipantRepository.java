package com.CodeForge.CodeForge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.User;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Long> {

    List<ContestParticipant> findByContest(Contest contest);

    List<ContestParticipant> findByUser(User user);

    Optional<ContestParticipant> findByContestAndUser(Contest contest, User user);

    List<ContestParticipant> findByContestOrderByScoreDesc(Contest contest);

    @Query("SELECT cp FROM ContestParticipant cp JOIN FETCH cp.user WHERE cp.contest = :contest")
    List<ContestParticipant> findByContestWithUser(@Param("contest") Contest contest);

    boolean existsByContestAndUser(Contest contest, User user);

     long countByContestId(Long contestId);

}
