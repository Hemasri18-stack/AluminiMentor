package com.alumini.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alumini.entity.MentorshipPair;
import com.alumini.service.MentorshipPairService;

@RestController
@RequestMapping("/api/mentorships")
public class MentorshipPairController {

    private final MentorshipPairService service;

    public MentorshipPairController(MentorshipPairService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MentorshipPair> create(
            @RequestParam Long studentId,
            @RequestParam Long alumniId) {
        return ResponseEntity.status(201).body(service.create(studentId, alumniId));
    }

    @GetMapping
    public List<MentorshipPair> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public MentorshipPair one(@PathVariable Long id) {
        return service.one(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggestions/student/{studentId}")
    public List<Map<String, Object>> suggestions(@PathVariable Long studentId) {
        return service.suggestions(studentId);
    }

    @GetMapping("/report")
    public List<Map<String, Object>> report() {
        return service.report();
    }

    @GetMapping("/report/monthly")
    public List<Map<String, Object>> monthlyReport() {
        return service.monthlyReport();
    }
}
