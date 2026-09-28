package com.mentorconnect.service;

import com.mentorconnect.dto.AlumniRequest;
import com.mentorconnect.entity.Alumni;
import com.mentorconnect.entity.InterestTag;
import com.mentorconnect.exception.BusinessException;
import com.mentorconnect.exception.ResourceNotFoundException;
import com.mentorconnect.repository.AlumniRepository;
import com.mentorconnect.repository.InterestTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AlumniService {

    private final AlumniRepository alumniRepository;
    private final InterestTagRepository interestTagRepository;

    public AlumniService(AlumniRepository alumniRepository,
                         InterestTagRepository interestTagRepository) {
        this.alumniRepository = alumniRepository;
        this.interestTagRepository = interestTagRepository;
    }

    @Transactional
    public Alumni create(AlumniRequest request) {
        if (alumniRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new BusinessException("An alumni with this email already exists");
        }

        Alumni alumni = new Alumni();
        mapRequest(alumni, request);
        return alumniRepository.save(alumni);
    }

    @Transactional(readOnly = true)
    public List<Alumni> getAll() {
        return alumniRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Alumni getById(Long id) {
        return alumniRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni not found with id: " + id));
    }

    @Transactional
    public Alumni update(Long id, AlumniRequest request) {
        Alumni alumni = getById(id);

        alumniRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Another alumni already uses this email");
            }
        });

        mapRequest(alumni, request);
        return alumniRepository.save(alumni);
    }

    @Transactional
    public void delete(Long id) {
        Alumni alumni = getById(id);
        alumniRepository.delete(alumni);
    }

    private void mapRequest(Alumni alumni, AlumniRequest request) {
        alumni.setName(request.getName());
        alumni.setEmail(request.getEmail());
        alumni.setMaxMentees(request.getMaxMentees());
        alumni.setAvailableSlots(request.getAvailableSlots());
        alumni.setExpertiseTags(resolveTags(request.getInterestTagIds()));
    }

    private Set<InterestTag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        List<InterestTag> tags = interestTagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new ResourceNotFoundException("One or more interest tag IDs do not exist");
        }
        return new HashSet<>(tags);
    }
}
