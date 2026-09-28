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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "alumni")
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    @Min(1)
    private Integer maxMentees;

    @ManyToMany
    @JoinTable(
        name = "alumni_interest_tags",
        joinColumns = @JoinColumn(name = "alumni_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @NotEmpty
    private Set<InterestTag> interestTags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "alumni_available_slots", joinColumns = @JoinColumn(name = "alumni_id"))
    @Column(name = "slot_key", nullable = false)
    @NotEmpty
    private Set<String> availableSlots = new HashSet<>();

    @OneToMany(mappedBy = "alumni")
    @JsonIgnore
    private Set<MentorshipPair> mentorshipPairs = new HashSet<>();

    public Alumni() {
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

    public Integer getMaxMentees() {
        return maxMentees;
    }

    public void setMaxMentees(Integer maxMentees) {
        this.maxMentees = maxMentees;
    }

    public Set<InterestTag> getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(Set<InterestTag> interestTags) {
        this.interestTags = interestTags;
    }

    public Set<String> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Set<String> availableSlots) {
        this.availableSlots = availableSlots;
    }
}
