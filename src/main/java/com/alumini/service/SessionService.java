package com.alumini.service;

import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.alumini.entity.MentorshipPair;
import com.alumini.entity.Session;
import com.alumini.exception.BusinessException;
import com.alumini.exception.ResourceNotFoundException;
import com.alumini.repository.MentorshipPairRepository;
import com.alumini.repository.SessionRepository;

@Service
public class SessionService {

    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("EEEE_HH:mm", Locale.ENGLISH);
    private static final List<String> SESSION_STATUSES = List.of("SCHEDULED", "COMPLETED", "CANCELLED");

    private final SessionRepository sessionRepository;
    private final MentorshipPairRepository mentorshipPairRepository;

    public SessionService(
            SessionRepository sessionRepository,
            MentorshipPairRepository mentorshipPairRepository) {
        this.sessionRepository = sessionRepository;
        this.mentorshipPairRepository = mentorshipPairRepository;
    }

    public Session create(Long mentorshipPairId, Session session) {
        MentorshipPair mentorshipPair = mentorshipPairRepository.findById(mentorshipPairId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship not found"));

        if (!"ACTIVE".equalsIgnoreCase(mentorshipPair.getStatus())) {
            throw new BusinessException("Mentorship is not active");
        }

        validateAvailability(mentorshipPair, session);
        if (session.getSessionDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("A session cannot be scheduled in the past");
        }
        boolean slotAlreadyBooked = sessionRepository.findAll().stream()
                .filter(existing -> "SCHEDULED".equalsIgnoreCase(existing.getStatus()))
                .filter(existing -> existing.getSessionDate().equals(session.getSessionDate()))
                .anyMatch(existing -> existing.getMentorshipPair().getStudent().getId()
                                .equals(mentorshipPair.getStudent().getId())
                        || existing.getMentorshipPair().getAlumni().getId()
                                .equals(mentorshipPair.getAlumni().getId()));
        if (slotAlreadyBooked) {
            throw new BusinessException("A participant already has a session at this time");
        }
        session.setMentorshipPair(mentorshipPair);
        session.setStatus(normalizeStatus(session.getStatus()));
        return sessionRepository.save(session);
    }

    public List<Session> all() {
        return sessionRepository.findAll();
    }

    public Session one(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    public Session update(Long id, Session updatedSession) {
        Session session = one(id);
        session.setTopic(updatedSession.getTopic());
        session.setSessionDate(updatedSession.getSessionDate());
        session.setDurationMinutes(updatedSession.getDurationMinutes());
        session.setStatus(normalizeStatus(updatedSession.getStatus()));
        validateAvailability(session.getMentorshipPair(), session);
        return sessionRepository.save(session);
    }

    private void validateAvailability(MentorshipPair pair, Session session) {
        if (session.getSessionDate().getMinute() != 0 || session.getSessionDate().getSecond() != 0) {
            throw new BusinessException("Sessions must start at the beginning of an available hour");
        }
        String slot = SLOT_FORMAT.format(session.getSessionDate()).toUpperCase(Locale.ROOT);
        if (!pair.getStudent().getPreferredSlots().contains(slot)
                || !pair.getAlumni().getAvailableSlots().contains(slot)) {
            throw new BusinessException("The selected time is not available for both participants");
        }
        if (session.getDurationMinutes() > 60) {
            throw new BusinessException("Sessions cannot exceed the one-hour availability slot");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "SCHEDULED" : status.trim().toUpperCase(Locale.ROOT);
        if (!SESSION_STATUSES.contains(normalized)) {
            throw new BusinessException("Session status must be SCHEDULED, COMPLETED, or CANCELLED");
        }
        return normalized;
    }

    public void delete(Long id) {
        sessionRepository.delete(one(id));
    }
}
