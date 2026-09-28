package com.alumini.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
            Set<String> studentSlots = student.getPreferredSlots();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Alumni alumni : alumniRepository.findAll()) {
            Set<String> matchingInterests = alumni.getInterestTags()
                    .stream()
                    .map(InterestTag::getName)
                    .filter(name -> studentInterests.contains(name.toLowerCase()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
                    Set<String> matchingSlots = alumni.getAvailableSlots()
                        .stream()
                        .filter(studentSlots::contains)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                    long activeMentees = mentorshipPairRepository
                        .countByAlumniIdAndStatus(alumni.getId(), "ACTIVE");

                        if (matchingInterests.isEmpty() || matchingSlots.isEmpty()
                            || mentorshipPairRepository.existsByAlumniIdAndStudentId(alumni.getId(), studentId)
                        || activeMentees >= alumni.getMaxMentees()) {
                    continue;
                    }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("alumniId", alumni.getId());
            item.put("name", alumni.getName());
            item.put("matchingScore", matchingInterests.size());
            item.put("matchingInterests", matchingInterests);
                item.put("matchingSlots", matchingSlots);
                item.put("currentMentees", activeMentees);
            item.put("maxMentees", alumni.getMaxMentees());

            result.add(item);
        }

        result.sort((first, second) -> {
            int interestComparison = Integer.compare(
                (Integer) second.get("matchingScore"),
                (Integer) first.get("matchingScore"));
            return interestComparison != 0 ? interestComparison : Integer.compare(
                ((Set<?>) second.get("matchingSlots")).size(),
                ((Set<?>) first.get("matchingSlots")).size());
        });

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

    public List<Map<String, Object>> monthlyReport() {
        Map<YearMonth, Map<String, Object>> months = new java.util.TreeMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (com.alumini.entity.Session session : sessionRepository.findAll()) {
            YearMonth month = YearMonth.from(session.getSessionDate());
            Map<String, Object> item = months.computeIfAbsent(month, key -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("month", key.format(monthFormatter));
                row.put("totalSessions", 0L);
                row.put("scheduledSessions", 0L);
                row.put("completedSessions", 0L);
                row.put("sessionsHeld", 0L);
                row.put("cancelledSessions", 0L);
                return row;
            });
            item.put("totalSessions", (Long) item.get("totalSessions") + 1);
            String status = session.getStatus();
            String counter = "COMPLETED".equalsIgnoreCase(status) ? "completedSessions"
                    : "CANCELLED".equalsIgnoreCase(status) ? "cancelledSessions" : "scheduledSessions";
            item.put(counter, (Long) item.get(counter) + 1);
            if ("COMPLETED".equalsIgnoreCase(status)) {
                item.put("sessionsHeld", (Long) item.get("sessionsHeld") + 1);
            }
        }

        return new ArrayList<>(months.values());
    }
}
