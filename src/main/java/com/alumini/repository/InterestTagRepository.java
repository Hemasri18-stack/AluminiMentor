package com.mentorconnect.repository;

import com.mentorconnect.entity.MentorshipPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MentorshipPairRepository extends JpaRepository<MentorshipPair, Long> {

    long countByAlumniIdAndStatusIn(Long alumniId, Collection<String> statuses);

    boolean existsByAlumniIdAndStudentId(Long alumniId, Long studentId);

    List<MentorshipPair> findByStudentId(Long studentId);

    List<MentorshipPair> findByAlumniId(Long alumniId);

    Optional<MentorshipPair> findByIdAndStatus(Long id, String status);
}
