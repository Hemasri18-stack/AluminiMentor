package com.alumini.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alumini.entity.Alumni;
import com.alumini.entity.InterestTag;
import com.alumini.exception.ResourceNotFoundException;
import com.alumini.repository.AlumniRepository;
import com.alumini.repository.InterestTagRepository;

@Service
public class AlumniService {

    private final AlumniRepository alumniRepository;
    private final InterestTagRepository tagRepository;

    public AlumniService(AlumniRepository alumniRepository, InterestTagRepository tagRepository) {
        this.alumniRepository = alumniRepository;
        this.tagRepository = tagRepository;
    }

    public Alumni create(Alumni alumni) {
        alumni.setInterestTags(resolveTags(alumni.getInterestTags()));
        return alumniRepository.save(alumni);
    }

    public List<Alumni> all() {
        return alumniRepository.findAll();
    }

    public Alumni one(Long id) {
        return alumniRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni not found"));
    }

    public Alumni update(Long id, Alumni updatedAlumni) {
        Alumni alumni = one(id);
        alumni.setName(updatedAlumni.getName());
        alumni.setEmail(updatedAlumni.getEmail());
        alumni.setMaxMentees(updatedAlumni.getMaxMentees());
        alumni.setInterestTags(resolveTags(updatedAlumni.getInterestTags()));
        alumni.setAvailableSlots(updatedAlumni.getAvailableSlots());
        return alumniRepository.save(alumni);
    }

    public void delete(Long id) {
        alumniRepository.delete(one(id));
    }

    private Set<InterestTag> resolveTags(Set<InterestTag> inputTags) {
        Set<InterestTag> result = new HashSet<>();

        if (inputTags == null) {
            return result;
        }

        for (InterestTag tag : inputTags) {
            if (tag.getId() != null) {
                result.add(tagRepository.findById(tag.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found")));
            } else {
                result.add(tagRepository.findByNameIgnoreCase(tag.getName())
                        .orElseGet(() -> tagRepository.save(new InterestTag(tag.getName()))));
            }
        }

        return result;
    }
}
