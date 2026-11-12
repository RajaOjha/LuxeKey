
package com.luxekey.backend.service;

import com.luxekey.backend.entity.*;
import com.luxekey.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final UserRepository users;

    public BookingService(BookingRepository bookings,
                          RoomRepository rooms,
                          UserRepository users) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.users = users;
    }

    public List<Room> getAvailableRooms(Long hotelId,
                                        LocalDate checkIn,
                                        LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        return rooms.findByHotelId(hotelId).stream()
            .filter(room -> !bookings.hasConflict(
                room.getId(), checkIn, checkOut))
            .toList();
    }

    @Transactional
    public Booking createBooking(Long roomId, Long customerId,
                                 LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        // Lock the room to prevent simultaneous bookings.
        Room room = bookings.lockRoom(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

        User customer = users.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!"CUSTOMER".equals(customer.getRole())) {
            throw new RuntimeException("Only customers can book rooms");
        }

        if (!room.getHotel().isApproved()) {
            throw new RuntimeException("Hotel is not approved");
        }

        if (bookings.hasConflict(roomId, checkIn, checkOut)) {
            throw new RuntimeException("Room is not available for these dates");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        Booking booking = new Booking();
        booking.setBookingReference(
            "LK-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase()
        );
        booking.setRoom(room);
        booking.setCustomer(customer);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setTotalPrice(room.getPrice() * nights);
        booking.setStatus("CONFIRMED");

        return bookings.save(booking);
    }

    @Transactional
    public Booking cancelBooking(String reference, String email) {
        Booking booking = bookings
            .findByBookingReferenceAndCustomer_Email(reference, email)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Lock the room before changing its booking status.
        bookings.lockRoom(booking.getRoom().getId())
            .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        return bookings.save(booking);
    }

    public Booking getBooking(String reference, String email) {
        return bookings.findByBookingReferenceAndCustomer_Email(reference, email)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null ||
            !checkIn.isBefore(checkOut) ||
            checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Invalid check-in or check-out dates");
        }
    }
}
