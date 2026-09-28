package com.mentorconnect.controller;

import com.mentorconnect.dto.AlumniRequest;
import com.mentorconnect.entity.Alumni;
import com.mentorconnect.service.AlumniService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @PostMapping
    public ResponseEntity<Alumni> create(@Valid @RequestBody AlumniRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alumniService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Alumni>> getAll() {
        return ResponseEntity.ok(alumniService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alumni> getById(@PathVariable Long id) {
        return ResponseEntity.ok(alumniService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alumni> update(@PathVariable Long id,
                                         @Valid @RequestBody AlumniRequest request) {
        return ResponseEntity.ok(alumniService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alumniService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
