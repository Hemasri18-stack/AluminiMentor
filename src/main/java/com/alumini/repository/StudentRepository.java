package com.alumini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alumini.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
