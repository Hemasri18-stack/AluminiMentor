package com.alumini.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alumini.entity.InterestTag;
import com.alumini.service.InterestTagService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tags")
public class InterestTagController {

    private final InterestTagService service;

    public InterestTagController(InterestTagService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InterestTag> create(@Valid @RequestBody InterestTag tag) {
        return ResponseEntity.status(201).body(service.create(tag));
    }

    @GetMapping
    public List<InterestTag> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public InterestTag one(@PathVariable Long id) {
        return service.one(id);
    }

    @PutMapping("/{id}")
    public InterestTag update(@PathVariable Long id, @Valid @RequestBody InterestTag tag) {
        return service.update(id, tag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
