package com.alumini.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alumini.entity.InterestTag;
import com.alumini.entity.Student;
import com.alumini.exception.ResourceNotFoundException;
import com.alumini.repository.InterestTagRepository;
import com.alumini.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final InterestTagRepository tagRepository;

    public StudentService(StudentRepository studentRepository, InterestTagRepository tagRepository) {
        this.studentRepository = studentRepository;
        this.tagRepository = tagRepository;
    }

    public Student create(Student student) {
        student.setInterestTags(resolveTags(student.getInterestTags()));
        return studentRepository.save(student);
    }

    public List<Student> all() {
        return studentRepository.findAll();
    }

    public Student one(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    public Student update(Long id, Student updatedStudent) {
        Student student = one(id);
        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());
        student.setDepartment(updatedStudent.getDepartment());
        student.setInterestTags(resolveTags(updatedStudent.getInterestTags()));
        return studentRepository.save(student);
    }

    public void delete(Long id) {
        studentRepository.delete(one(id));
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
