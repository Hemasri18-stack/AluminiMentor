package com.alumini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alumini.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    long countByMentorshipPairId(Long id);
}
