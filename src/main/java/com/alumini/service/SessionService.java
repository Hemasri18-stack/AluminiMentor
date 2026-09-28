package com.mentorconnect.service;

import com.mentorconnect.entity.InterestTag;
import com.mentorconnect.exception.BusinessException;
import com.mentorconnect.exception.ResourceNotFoundException;
import com.mentorconnect.repository.InterestTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterestTagService {

    private final InterestTagRepository repository;

    public InterestTagService(InterestTagRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InterestTag create(InterestTag tag) {
        String name = tag.getName().trim();
        if (repository.findByNameIgnoreCase(name).isPresent()) {
            throw new BusinessException("Interest tag already exists: " + name);
        }
        tag.setName(name);
        return repository.save(tag);
    }

    @Transactional(readOnly = true)
    public List<InterestTag> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public InterestTag getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest tag not found with id: " + id));
    }

    @Transactional
    public InterestTag update(Long id, InterestTag request) {
        InterestTag tag = getById(id);
        String name = request.getName().trim();

        repository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Interest tag already exists: " + name);
            }
        });

        tag.setName(name);
        return repository.save(tag);
    }

    @Transactional
    public void delete(Long id) {
        InterestTag tag = getById(id);
        repository.delete(tag);
    }
}
