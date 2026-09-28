package com.alumini.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alumini.entity.Alumni;
import com.alumini.entity.InterestTag;
import com.alumini.entity.MentorshipPair;
import com.alumini.entity.Student;
import com.alumini.exception.BusinessException;
import com.alumini.exception.ResourceNotFoundException;
import com.alumini.repository.AlumniRepository;
import com.alumini.repository.MentorshipPairRepository;
import com.alumini.repository.SessionRepository;
import com.alumini.repository.StudentRepository;

@Service
public class MentorshipPairService {

    private final MentorshipPairRepository mentorshipPairRepository;
    private final AlumniRepository alumniRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;

    public MentorshipPairService(
            MentorshipPairRepository mentorshipPairRepository,
            AlumniRepository alumniRepository,
            StudentRepository studentRepository,
            SessionRepository sessionRepository) {
        this.mentorshipPairRepository = mentorshipPairRepository;
        this.alumniRepository = alumniRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
    }

    public MentorshipPair create(Long studentId, Long alumniId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Alumni alumni = alumniRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni not found"));

        if (mentorshipPairRepository.existsByAlumniIdAndStudentId(alumniId, studentId)) {
            throw new BusinessException("Mentorship already exists");
        }

        long activeMentees = mentorshipPairRepository
                .countByAlumniIdAndStatus(alumniId, "ACTIVE");

        if (activeMentees >= alumni.getMaxMentees()) {
            throw new BusinessException("Mentor has reached maximum mentee capacity");
        }

        MentorshipPair pair = new MentorshipPair();
        pair.setStudent(student);
        pair.setAlumni(alumni);

        return mentorshipPairRepository.save(pair);
    }

    public List<MentorshipPair> all() {
        return mentorshipPairRepository.findAll();
    }

    public MentorshipPair one(Long id) {
        return mentorshipPairRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship not found"));
    }

    public void delete(Long id) {
        mentorshipPairRepository.delete(one(id));
    }

    public List<Map<String, Object>> suggestions(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Set<String> studentInterests = student.getInterestTags()
                .stream()
                .map(tag -> tag.getName().toLowerCase())
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();

        for (Alumni alumni : alumniRepository.findAll()) {
            Set<String> matchingInterests = alumni.getInterestTags()
                    .stream()
                    .map(InterestTag::getName)
                    .filter(name -> studentInterests.contains(name.toLowerCase()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("alumniId", alumni.getId());
            item.put("name", alumni.getName());
            item.put("matchingScore", matchingInterests.size());
            item.put("matchingInterests", matchingInterests);
            item.put("currentMentees",
                    mentorshipPairRepository.countByAlumniIdAndStatus(alumni.getId(), "ACTIVE"));
            item.put("maxMentees", alumni.getMaxMentees());

            result.add(item);
        }

        result.sort((first, second) -> Integer.compare(
                (Integer) second.get("matchingScore"),
                (Integer) first.get("matchingScore")));

        return result;
    }

    public List<Map<String, Object>> report() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (MentorshipPair pair : mentorshipPairRepository.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mentorshipId", pair.getId());
            item.put("alumni", pair.getAlumni().getName());
            item.put("student", pair.getStudent().getName());
            item.put("status", pair.getStatus());
            item.put("totalSessions", sessionRepository.countByMentorshipPairId(pair.getId()));
            result.add(item);
        }

        return result;
    }
}
