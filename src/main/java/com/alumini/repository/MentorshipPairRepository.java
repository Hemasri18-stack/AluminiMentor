package com.alumini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alumini.entity.MentorshipPair;

public interface MentorshipPairRepository extends JpaRepository<MentorshipPair, Long> {

    long countByAlumniIdAndStatus(Long id, String status);

    boolean existsByAlumniIdAndStudentId(Long alumniId, Long studentId);
}
