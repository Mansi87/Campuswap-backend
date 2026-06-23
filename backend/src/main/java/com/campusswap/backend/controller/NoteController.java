package com.campusswap.backend.controller;

import com.campusswap.backend.service.FileUploadService;
import com.campusswap.backend.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final FileUploadService fileUploadService;

    // Upload PDF
    @PostMapping("/upload-pdf")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadService.uploadPdf(file, "notes");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Create note
    @PostMapping
    public ResponseEntity<?> createNote(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String subject,
            @RequestParam String semester,
            @RequestParam(required = false) List<String> previewImages,
            @RequestParam String pdfUrl,
            @RequestParam boolean isFree,
            @RequestParam(required = false, defaultValue = "0") BigDecimal price,
            Authentication authentication) {
        try {
            var note = noteService.createNote(
                    authentication.getName(), title, description,
                    subject, semester, previewImages, pdfUrl, isFree, price);
            return ResponseEntity.ok(Map.of("id", note.getId(), "message", "Note created!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all college notes
    @GetMapping
    public ResponseEntity<?> getCollegeNotes(Authentication authentication) {
        return ResponseEntity.ok(noteService.getCollegeNotes(authentication.getName()));
    }

    // Get single note
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable String id, Authentication authentication) {
        try {
            return ResponseEntity.ok(noteService.getNoteById(
                    UUID.fromString(id), authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get my notes
    @GetMapping("/my-notes")
    public ResponseEntity<?> getMyNotes(Authentication authentication) {
        return ResponseEntity.ok(noteService.getMyNotes(authentication.getName()));
    }

    // Download note (locked behind purchase check)
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadNote(@PathVariable String id, Authentication authentication) {
        try {
            String url = noteService.getDownloadUrl(
                    UUID.fromString(id), authentication.getName());
            return ResponseEntity.ok(Map.of("downloadUrl", url));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
