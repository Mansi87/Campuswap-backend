package com.campusswap.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.campusswap.backend.model.*;
import com.campusswap.backend.repository.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final PurchaseRepository purchaseRepository;

    public Note createNote(String userEmail, String title, String description,
                           String subject, String semester,
                           List<String> previewImages, String pdfUrl,
                           boolean isFree, BigDecimal price) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Note note = new Note();
        note.setSeller(user);
        note.setCollege(user.getCollege());
        note.setTitle(title);
        note.setDescription(description);
        note.setSubject(subject);
        note.setSemester(semester);
        note.setPreviewImages(previewImages);
        note.setPdfUrl(pdfUrl);
        note.setFree(isFree);
        note.setPrice(isFree ? BigDecimal.ZERO : price);

        return noteRepository.save(note);
    }

    public List<Map<String, Object>> getCollegeNotes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return noteRepository.findByCollegeOrderByCreatedAtDesc(user.getCollege())
                .stream()
                .map(note -> mapNote(note, user))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getNoteById(UUID noteId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return mapNote(note, user);
    }

    public List<Map<String, Object>> getMyNotes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return noteRepository.findBySellerOrderByCreatedAtDesc(user)
                .stream()
                .map(note -> mapNote(note, user))
                .collect(Collectors.toList());
    }

    public String getDownloadUrl(UUID noteId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Free notes — anyone can download
        if (note.isFree()) return note.getPdfUrl();

        // Paid notes — check purchase
        boolean purchased = purchaseRepository.existsByBuyerAndNote(user, note);
        if (purchased) return note.getPdfUrl();

        // Seller can always download their own notes
        if (note.getSeller().getId().equals(user.getId())) return note.getPdfUrl();

        throw new RuntimeException("Purchase required to download this note");
    }

    private Map<String, Object> mapNote(Note note, User currentUser) {
        boolean purchased = purchaseRepository.existsByBuyerAndNote(currentUser, note);
        boolean isOwner = note.getSeller().getId().equals(currentUser.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("id", note.getId().toString());
        map.put("title", note.getTitle());
        map.put("description", note.getDescription() != null ? note.getDescription() : "");
        map.put("subject", note.getSubject());
        map.put("semester", note.getSemester());
        map.put("previewImages", note.getPreviewImages() != null ? note.getPreviewImages() : List.of());
        map.put("isFree", note.isFree());
        map.put("price", note.getPrice());
        map.put("sellerName", note.getSeller().getFullName());
        map.put("sellerId", note.getSeller().getId().toString());
        map.put("collegeName", note.getCollege().getName());
        map.put("createdAt", note.getCreatedAt().toString());
        map.put("canDownload", note.isFree() || purchased || isOwner);
        map.put("isPurchased", purchased);
        return map;
    }
}
