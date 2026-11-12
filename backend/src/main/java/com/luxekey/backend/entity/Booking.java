
package com.luxekey.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import jakarta.persistence.Column;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private double totalPrice;
    private String status;

    @Column(unique = true)
    private String bookingReference;

    @ManyToOne
    private User customer;

    @ManyToOne
    private Room room;
}
