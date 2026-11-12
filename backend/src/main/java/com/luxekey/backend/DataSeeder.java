
package com.luxekey.backend;

import com.luxekey.backend.entity.Hotel;
import com.luxekey.backend.entity.Room;
import com.luxekey.backend.repository.HotelRepository;
import com.luxekey.backend.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedHotels(HotelRepository hotels, RoomRepository rooms) {
        return args -> {
            if (hotels.count() > 0) return;

            String[][] data = {
                {"The Grand Palace", "Mumbai", "Luxury city hotel near major attractions"},
                {"Ocean Breeze Resort", "Goa", "Beachside resort with relaxing sea views"},
                {"Royal Heritage Inn", "Jaipur", "Traditional stay with royal architecture"},
                {"Mountain View Retreat", "Manali", "Peaceful mountain hotel surrounded by nature"},
                {"Urban Comfort Suites", "Pune", "Modern hotel for business and leisure"}
            };

            String[][] amenities = {
                {"WiFi", "Swimming Pool", "Restaurant", "Gym"},
                {"WiFi", "Beach Access", "Breakfast", "Swimming Pool"},
                {"WiFi", "Restaurant", "Parking", "Spa"},
                {"WiFi", "Mountain View", "Restaurant", "Parking"},
                {"WiFi", "Gym", "Restaurant", "Conference Room"}
            };

            for (int i = 0; i < data.length; i++) {
                Hotel hotel = new Hotel();
                hotel.setName(data[i][0]);
                hotel.setLocation(data[i][1]);
                hotel.setDescription(data[i][2]);
                hotel.setApproved(true);
                hotel.setAmenities(List.of(amenities[i]));

                hotel = hotels.save(hotel);

                Room room = new Room();
                room.setRoomType("Deluxe Room");
                room.setPrice(3500 + (i * 1000));
                room.setCapacity(2);
                room.setHotel(hotel);

                rooms.save(room);
            }
        };
    }
}
