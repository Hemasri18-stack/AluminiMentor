package com.alumini.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alumini.entity.MentorshipPair;
import com.alumini.entity.Session;
import com.alumini.exception.BusinessException;
import com.alumini.exception.ResourceNotFoundException;
import com.alumini.repository.MentorshipPairRepository;
import com.alumini.repository.SessionRepository;

@Service
public class SessionService {

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

        session.setMentorshipPair(mentorshipPair);
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
        return sessionRepository.save(session);
    }

    public void delete(Long id) {
        sessionRepository.delete(one(id));
    }
}
