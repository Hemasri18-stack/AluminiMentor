package com.alumini.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "interest_tags")
public class InterestTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @ManyToMany(mappedBy = "interestTags")
    @JsonIgnore
    private Set<Alumni> alumni = new HashSet<>();

    @ManyToMany(mappedBy = "interestTags")
    @JsonIgnore
    private Set<Student> students = new HashSet<>();

    public InterestTag() {
    }

    public InterestTag(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Alumni> getAlumni() {
        return alumni;
    }

    public Set<Student> getStudents() {
        return students;
    }
}
