package com.alumini.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "mentorship_pairs")
public class MentorshipPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Alumni alumni;

    @ManyToOne(optional = false)
    private Student student;

    private String status = "ACTIVE";

    private LocalDate startDate = LocalDate.now();

    @OneToMany(mappedBy = "mentorshipPair")
    @JsonIgnore
    private Set<Session> sessions = new HashSet<>();

    public MentorshipPair() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alumni getAlumni() {
        return alumni;
    }

    public void setAlumni(Alumni alumni) {
        this.alumni = alumni;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Set<Session> getSessions() {
        return sessions;
    }
}
