
package com.luxekey.backend.controller;

import com.luxekey.backend.entity.*;
import com.luxekey.backend.repository.*;
import com.luxekey.backend.service.BookingService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class HotelController {

    private final HotelRepository hotels;
    private final RoomRepository rooms;
    private final BookingService bookingService;

    public HotelController(HotelRepository hotels, RoomRepository rooms,
                           BookingService bookingService) {
        this.hotels = hotels;
        this.rooms = rooms;
        this.bookingService = bookingService;
    }

    @GetMapping("/hotels")
    public List<Hotel> getHotels() {
        return hotels.findByApprovedTrue();
    }

    @GetMapping("/hotels/search")
    public List<Hotel> searchHotels(@RequestParam String location) {
        return hotels.findByLocationContainingIgnoreCaseAndApprovedTrue(location);
    }

    @GetMapping("/hotels/{id}")
    public Hotel getHotel(@PathVariable Long id) {
        return hotels.findById(id)
            .filter(Hotel::isApproved)
            .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    @GetMapping("/hotels/{id}/rooms")
    public List<Room> getRooms(@PathVariable Long id) {
        getHotel(id);
        return rooms.findByHotelId(id);
    }

    @GetMapping("/hotels/{id}/availability")
    public List<Room> getAvailableRooms(
        @PathVariable Long id,
        @RequestParam LocalDate checkIn,
        @RequestParam LocalDate checkOut) {

        getHotel(id);
        return bookingService.getAvailableRooms(id, checkIn, checkOut);
    }

    @PostMapping("/bookings")
    public Booking createBooking(
        @RequestParam Long roomId,
        @RequestParam Long customerId,
        @RequestParam LocalDate checkIn,
        @RequestParam LocalDate checkOut) {

        return bookingService.createBooking(
            roomId, customerId, checkIn, checkOut);
    }

    @GetMapping("/bookings/{reference}")
    public Booking getBooking(
        @PathVariable String reference,
        @RequestParam String email) {

        return bookingService.getBooking(reference, email);
    }

    @PutMapping("/bookings/{reference}/cancel")
    public Booking cancelBooking(
        @PathVariable String reference,
        @RequestParam String email) {

        return bookingService.cancelBooking(reference, email);
    }
}
