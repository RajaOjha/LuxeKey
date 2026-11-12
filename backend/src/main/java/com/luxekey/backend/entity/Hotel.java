package com.luxekey.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @Column(length = 1000)
    private String description;

    private boolean approved = false;

    @ElementCollection
    private List<String> amenities;

    @ManyToOne
    private User manager;
}