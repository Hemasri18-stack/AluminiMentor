package com.alumini.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String department;

    @ManyToMany
    @JoinTable(
        name = "student_interest_tags",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @NotEmpty
    private Set<InterestTag> interestTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "student_preferred_slots", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "slot_key", nullable = false)
    @NotEmpty
    private Set<String> preferredSlots = new HashSet<>();

    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private Set<MentorshipPair> mentorshipPairs = new HashSet<>();

    public Student() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Set<InterestTag> getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(Set<InterestTag> interestTags) {
        this.interestTags = interestTags;
    }

    public Set<String> getPreferredSlots() {
        return preferredSlots;
    }

    public void setPreferredSlots(Set<String> preferredSlots) {
        this.preferredSlots = preferredSlots;
    }
}
