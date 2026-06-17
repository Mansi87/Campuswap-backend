package com.campusswap.backend.controller;

import com.campusswap.backend.model.College;
import com.campusswap.backend.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    private final CollegeRepository collegeRepository;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from CampusSwap Backend!");
    }

    @GetMapping("/colleges")
    public ResponseEntity<List<College>> getColleges(){
        return ResponseEntity.ok(collegeRepository.findAll());
    }
}
