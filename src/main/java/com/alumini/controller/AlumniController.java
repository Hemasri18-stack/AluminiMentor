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

import com.alumini.entity.Alumni;
import com.alumini.service.AlumniService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alumni")
public class AlumniController {

    private final AlumniService service;

    public AlumniController(AlumniService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Alumni> create(@Valid @RequestBody Alumni alumni) {
        return ResponseEntity.status(201).body(service.create(alumni));
    }

    @GetMapping
    public List<Alumni> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Alumni one(@PathVariable Long id) {
        return service.one(id);
    }

    @PutMapping("/{id}")
    public Alumni update(@PathVariable Long id, @Valid @RequestBody Alumni alumni) {
        return service.update(id, alumni);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
