
package com.luxekey.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType;
    private double price;
    private int capacity;

    @ManyToOne
    private Hotel hotel;
}
