package com.mentorconnect.service;

import com.mentorconnect.dto.SessionRequest;
import com.mentorconnect.entity.MentorshipPair;
import com.mentorconnect.entity.Session;
import com.mentorconnect.exception.BusinessException;
import com.mentorconnect.exception.ResourceNotFoundException;
import com.mentorconnect.repository.MentorshipPairRepository;
import com.mentorconnect.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MentorshipPairRepository mentorshipPairRepository;

    public SessionService(SessionRepository sessionRepository,
                          MentorshipPairRepository mentorshipPairRepository) {
        this.sessionRepository = sessionRepository;
        this.mentorshipPairRepository = mentorshipPairRepository;
    }

    @Transactional
    public Session create(SessionRequest request) {
        MentorshipPair pair = getPair(request.getMentorshipPairId());

        if (!"ACTIVE".equals(pair.getStatus())) {
            throw new BusinessException("A mentoring session can only be created for an ACTIVE mentorship pair");
        }

        Session session = new Session();
        mapRequest(session, request, pair);
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<Session> getAll() {
        return sessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Session getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Session> getByMentorshipPair(Long pairId) {
        getPair(pairId);
        return sessionRepository.findByMentorshipPairId(pairId);
    }

    @Transactional
    public Session update(Long id, SessionRequest request) {
        Session session = getById(id);
        MentorshipPair pair = getPair(request.getMentorshipPairId());

        if (!"ACTIVE".equals(pair.getStatus())) {
            throw new BusinessException("A mentoring session can only belong to an ACTIVE mentorship pair");
        }

        mapRequest(session, request, pair);
        return sessionRepository.save(session);
    }

    @Transactional
    public void delete(Long id) {
        Session session = getById(id);
        sessionRepository.delete(session);
    }

    private MentorshipPair getPair(Long id) {
        return mentorshipPairRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship pair not found with id: " + id));
    }

    private void mapRequest(Session session, SessionRequest request, MentorshipPair pair) {
        session.setMentorshipPair(pair);
        session.setSessionDate(request.getSessionDate());
        session.setDurationMinutes(request.getDurationMinutes());
        session.setTopic(request.getTopic());
        session.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? "SCHEDULED"
                : request.getStatus().trim().toUpperCase());
    }
}
