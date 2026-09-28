package com.alumini.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alumini.entity.Alumni;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {
}
