
package com.luxekey.backend.repository;

import com.luxekey.backend.entity.Booking;
import com.luxekey.backend.entity.Room;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> lockRoom(@Param("roomId") Long roomId);

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status = 'CONFIRMED'
        AND b.checkIn < :checkOut
        AND b.checkOut > :checkIn
    """)
    boolean hasConflict(
        @Param("roomId") Long roomId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    List<Booking> findByCustomerId(Long customerId);

    Optional<Booking> findByBookingReferenceAndCustomer_Email(
        String bookingReference, String email
    );
}
