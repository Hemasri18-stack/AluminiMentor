package com.alumini.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alumini.entity.InterestTag;

public interface InterestTagRepository extends JpaRepository<InterestTag, Long> {

    Optional<InterestTag> findByNameIgnoreCase(String name);
}
