package com.mentorconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "mentorship_pairs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"alumni_id", "student_id"})
)
public class MentorshipPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "alumni_id", nullable = false)
    private Alumni alumni;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private String status;

    public MentorshipPair() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alumni getAlumni() { return alumni; }
    public void setAlumni(Alumni alumni) { this.alumni = alumni; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
