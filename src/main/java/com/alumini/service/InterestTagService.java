package com.alumini.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alumini.entity.InterestTag;
import com.alumini.exception.BusinessException;
import com.alumini.exception.ResourceNotFoundException;
import com.alumini.repository.InterestTagRepository;

@Service
public class InterestTagService {

    private final InterestTagRepository repository;

    public InterestTagService(InterestTagRepository repository) {
        this.repository = repository;
    }

    public InterestTag create(InterestTag tag) {
        if (repository.findByNameIgnoreCase(tag.getName()).isPresent()) {
            throw new BusinessException("Interest tag already exists");
        }

        return repository.save(tag);
    }

    public List<InterestTag> all() {
        return repository.findAll();
    }

    public InterestTag one(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest tag not found"));
    }

    public InterestTag update(Long id, InterestTag updatedTag) {
        InterestTag tag = one(id);
        tag.setName(updatedTag.getName());
        return repository.save(tag);
    }

    public void delete(Long id) {
        repository.delete(one(id));
    }
}
