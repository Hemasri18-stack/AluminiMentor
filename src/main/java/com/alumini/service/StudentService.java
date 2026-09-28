package com.mentorconnect.service;

import com.mentorconnect.dto.AlumniMatchResponse;
import com.mentorconnect.dto.MentorshipReportResponse;
import com.mentorconnect.dto.MentorshipRequest;
import com.mentorconnect.entity.Alumni;
import com.mentorconnect.entity.InterestTag;
import com.mentorconnect.entity.MentorshipPair;
import com.mentorconnect.entity.Student;
import com.mentorconnect.exception.BusinessException;
import com.mentorconnect.exception.ResourceNotFoundException;
import com.mentorconnect.repository.AlumniRepository;
import com.mentorconnect.repository.MentorshipPairRepository;
import com.mentorconnect.repository.SessionRepository;
import com.mentorconnect.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MentorshipPairService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("PENDING", "ACTIVE");
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "ACTIVE", "COMPLETED", "REJECTED");

    private final MentorshipPairRepository mentorshipPairRepository;
    private final AlumniRepository alumniRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;

    public MentorshipPairService(MentorshipPairRepository mentorshipPairRepository,
                                 AlumniRepository alumniRepository,
                                 StudentRepository studentRepository,
                                 SessionRepository sessionRepository) {
        this.mentorshipPairRepository = mentorshipPairRepository;
        this.alumniRepository = alumniRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public MentorshipPair create(MentorshipRequest request) {
        Alumni alumni = getAlumni(request.getAlumniId());
        Student student = getStudent(request.getStudentId());

        if (mentorshipPairRepository.existsByAlumniIdAndStudentId(alumni.getId(), student.getId())) {
            throw new BusinessException("This student and alumni already have a mentorship relationship");
        }

        long currentMentees = mentorshipPairRepository
                .countByAlumniIdAndStatusIn(alumni.getId(), ACTIVE_STATUSES);

        if (currentMentees >= alumni.getMaxMentees()) {
            throw new BusinessException(
                    "Mentor has reached the maximum concurrent mentee limit of " + alumni.getMaxMentees());
        }

        MentorshipPair pair = new MentorshipPair();
        pair.setAlumni(alumni);
        pair.setStudent(student);
        pair.setStartDate(LocalDate.now());
        pair.setStatus("ACTIVE");

        return mentorshipPairRepository.save(pair);
    }

    @Transactional(readOnly = true)
    public List<MentorshipPair> getAll() {
        return mentorshipPairRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MentorshipPair getById(Long id) {
        return mentorshipPairRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship pair not found with id: " + id));
    }

    @Transactional
    public MentorshipPair updateStatus(Long id, String status) {
        MentorshipPair pair = getById(id);
        String normalized = status == null ? "" : status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(normalized)) {
            throw new BusinessException("Invalid mentorship status. Use PENDING, ACTIVE, COMPLETED or REJECTED");
        }

        if ("ACTIVE".equals(normalized) && !"ACTIVE".equals(pair.getStatus())) {
            Alumni alumni = pair.getAlumni();
            long currentMentees = mentorshipPairRepository
                    .countByAlumniIdAndStatusIn(alumni.getId(), ACTIVE_STATUSES);
            if (currentMentees >= alumni.getMaxMentees()) {
                throw new BusinessException(
                        "Mentor has reached the maximum concurrent mentee limit of " + alumni.getMaxMentees());
            }
        }

        pair.setStatus(normalized);
        return mentorshipPairRepository.save(pair);
    }

    @Transactional
    public void delete(Long id) {
        MentorshipPair pair = getById(id);
        mentorshipPairRepository.delete(pair);
    }

    @Transactional(readOnly = true)
    public List<MentorshipPair> getByStudent(Long studentId) {
        getStudent(studentId);
        return mentorshipPairRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<MentorshipPair> getByAlumni(Long alumniId) {
        getAlumni(alumniId);
        return mentorshipPairRepository.findByAlumniId(alumniId);
    }

    @Transactional(readOnly = true)
    public List<AlumniMatchResponse> suggestMentors(Long studentId) {
        Student student = getStudent(studentId);
        Set<String> studentTags = new HashSet<>();
        for (InterestTag tag : student.getInterestTags()) {
            studentTags.add(normalize(tag.getName()));
        }

        List<AlumniMatchResponse> matches = new ArrayList<>();

        for (Alumni alumni : alumniRepository.findAll()) {
            List<String> matchingTags = new ArrayList<>();

            for (InterestTag tag : alumni.getExpertiseTags()) {
                if (studentTags.contains(normalize(tag.getName()))) {
                    matchingTags.add(tag.getName());
                }
            }

            long currentMentees = mentorshipPairRepository
                    .countByAlumniIdAndStatusIn(alumni.getId(), ACTIVE_STATUSES);

            boolean available = currentMentees < alumni.getMaxMentees();

            matches.add(new AlumniMatchResponse(
                    alumni.getId(),
                    alumni.getName(),
                    alumni.getEmail(),
                    alumni.getMaxMentees(),
                    currentMentees,
                    available,
                    matchingTags.size(),
                    matchingTags
            ));
        }

        matches.sort(Comparator
                .comparingInt(AlumniMatchResponse::getMatchingTagCount)
                .reversed()
                .thenComparing(AlumniMatchResponse::getName, String.CASE_INSENSITIVE_ORDER));

        return matches;
    }

    @Transactional(readOnly = true)
    public List<MentorshipReportResponse> getEngagementReport() {
        List<MentorshipReportResponse> report = new ArrayList<>();

        for (MentorshipPair pair : mentorshipPairRepository.findAll()) {
            long sessionCount = sessionRepository.findByMentorshipPairId(pair.getId()).size();

            report.add(new MentorshipReportResponse(
                    pair.getId(),
                    pair.getAlumni().getId(),
                    pair.getAlumni().getName(),
                    pair.getStudent().getId(),
                    pair.getStudent().getName(),
                    pair.getStatus(),
                    sessionCount
            ));
        }

        return report;
    }

    private Alumni getAlumni(Long id) {
        return alumniRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni not found with id: " + id));
    }

    private Student getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
