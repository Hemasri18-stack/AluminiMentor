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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alumini.entity.Session;
import com.alumini.service.SessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Session> create(
            @RequestParam Long mentorshipPairId,
            @Valid @RequestBody Session session) {
        return ResponseEntity.status(201).body(service.create(mentorshipPairId, session));
    }

    @GetMapping
    public List<Session> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Session one(@PathVariable Long id) {
        return service.one(id);
    }

    @PutMapping("/{id}")
    public Session update(@PathVariable Long id, @Valid @RequestBody Session session) {
        return service.update(id, session);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
