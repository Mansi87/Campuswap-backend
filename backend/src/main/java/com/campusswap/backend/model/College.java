package com.campusswap.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="colleges")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class College {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String emailDomain;

    private String location;

    private String city;

    private Boolean isActive = true;
}
