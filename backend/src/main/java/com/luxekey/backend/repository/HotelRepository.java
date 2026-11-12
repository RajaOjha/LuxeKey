package com.luxekey.backend.repository;

import com.luxekey.backend.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByApprovedTrue();

    List<Hotel> findByLocationContainingIgnoreCaseAndApprovedTrue(String location);
}