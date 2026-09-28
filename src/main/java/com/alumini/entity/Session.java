package com.alumini.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnoreProperties("sessions")
    private MentorshipPair mentorshipPair;

    @NotBlank
    private String topic;

    @NotNull
    private LocalDateTime sessionDate;

    @NotNull
    @Positive
    private Integer durationMinutes;

    public Session() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MentorshipPair getMentorshipPair() {
        return mentorshipPair;
    }

    public void setMentorshipPair(MentorshipPair mentorshipPair) {
        this.mentorshipPair = mentorshipPair;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
