package com.mentorconnect.controller;

import com.mentorconnect.entity.InterestTag;
import com.mentorconnect.service.InterestTagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class InterestTagController {

    private final InterestTagService interestTagService;

    public InterestTagController(InterestTagService interestTagService) {
        this.interestTagService = interestTagService;
    }

    @PostMapping
    public ResponseEntity<InterestTag> create(@Valid @RequestBody InterestTag tag) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interestTagService.create(tag));
    }

    @GetMapping
    public ResponseEntity<List<InterestTag>> getAll() {
        return ResponseEntity.ok(interestTagService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterestTag> getById(@PathVariable Long id) {
        return ResponseEntity.ok(interestTagService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterestTag> update(@PathVariable Long id,
                                              @Valid @RequestBody InterestTag tag) {
        return ResponseEntity.ok(interestTagService.update(id, tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interestTagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
