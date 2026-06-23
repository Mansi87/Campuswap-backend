package com.campusswap.backend.repository;


import com.campusswap.backend.model.College;
import com.campusswap.backend.model.Note;
import com.campusswap.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByCollegeOrderByCreatedAtDesc(College college);
    List<Note> findBySellerOrderByCreatedAtDesc(User seller);
}
