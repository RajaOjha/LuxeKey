
import { useEffect, useState } from "react";
import axios from "axios";
import {
  Search, MapPin, CalendarDays, Hotel, ArrowRight
} from "lucide-react";
import "./App.css";

const API = "http://localhost:8080/api";
const CUSTOMER_ID = 1;

export default function App() {
  const [hotels, setHotels] = useState([]);
  const [location, setLocation] = useState("");
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [selected, setSelected] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [message, setMessage] = useState("");
  const [customerName, setCustomerName] = useState("");
  const [customerEmail, setCustomerEmail] = useState("");
  const [bookingRoom, setBookingRoom] = useState(null);
  const [confirmedBooking, setConfirmedBooking] = useState(null);
  const [loading, setLoading] = useState(false);

  const loadHotels = async () => {
    try {
      setLoading(true);

      const url = location.trim()
        ? `${API}/hotels/search?location=${encodeURIComponent(location)}`
        : `${API}/hotels`;

      const res = await axios.get(url);
      setHotels(res.data);
      setMessage("");
    } catch {
      setMessage("Unable to load hotels. Check the backend.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHotels();
  }, []);

  const viewRooms = async (hotel) => {
    setSelected(hotel);
    setBookingRoom(null);
    setMessage("");

    try {
      let url = `${API}/hotels/${hotel.id}/rooms`;

      if (checkIn || checkOut) {
        if (!checkIn || !checkOut || checkIn >= checkOut) {
          setRooms([]);
          setMessage("Please select valid check-in and check-out dates.");
          return;
        }

        url = `${API}/hotels/${hotel.id}/availability?checkIn=${checkIn}&checkOut=${checkOut}`;
      }

      const res = await axios.get(url);
      setRooms(res.data);
    } catch {
      setRooms([]);
      setMessage("Unable to load rooms.");
    }
  };

  const nights = checkIn && checkOut
    ? Math.max(
        0,
        (new Date(checkOut) - new Date(checkIn)) / 86400000
      )
    : 1;

  const confirmBooking = async () => {
    if (!customerName.trim() || !customerEmail.trim()) {
      setMessage("Please enter your name and email.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customerEmail)) {
      setMessage("Please enter a valid email address.");
      return;
    }

    if (!checkIn || !checkOut || checkIn >= checkOut) {
      setMessage("Please select valid booking dates.");
      return;
    }

    const today = new Date();
    const todayString = [
      today.getFullYear(),
      String(today.getMonth() + 1).padStart(2, "0"),
      String(today.getDate()).padStart(2, "0")
    ].join("-");

    if (checkIn < todayString) {
      setMessage("Check-in date cannot be in the past.");
      return;
    }

    try {
      setLoading(true);
      setMessage("");

      const res = await axios.post(`${API}/bookings`, null, {
        params: {
          roomId: bookingRoom.id,
          customerId: CUSTOMER_ID,
          checkIn,
          checkOut
        }
      });

      setConfirmedBooking(res.data);
      setBookingRoom(null);
      setSelected(null);
      setCustomerName("");
      setCustomerEmail("");
      setMessage("");

    } catch (error) {
      setMessage(
        error.response?.data?.message ||
        "Booking failed. Please check availability and try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main>
      <nav className="nav">
        <h2>Luxe<span>Key.</span></h2>

        <div>
          Discover <span>•</span> Stays <span>•</span> Experiences
        </div>

        <button
          className="outline"
          onClick={() =>
            document.querySelector(".results")?.scrollIntoView({
              behavior: "smooth"
            })
          }
        >
          Explore stays
        </button>
      </nav>

      <section className="hero">
        <p className="eyebrow">THE ART OF TRAVEL</p>

        <h1>
          Find your place<br />in the <i>world.</i>
        </h1>

        <p className="sub">
          Exceptional stays. Thoughtful experiences. Unforgettable journeys.
        </p>

        <div className="search">
          <label>
            <MapPin size={17}/> DESTINATION
            <input
              value={location}
              onChange={e => setLocation(e.target.value)}
              placeholder="Where to?"
            />
          </label>

          <label>
            <CalendarDays size={17}/> CHECK IN
            <input
              type="date"
              min={new Date().toLocaleDateString("en-CA")}
              value={checkIn}
              onChange={e => setCheckIn(e.target.value)}
            />
          </label>

          <label>
            <CalendarDays size={17}/> CHECK OUT
            <input
              type="date"
              min={checkIn || new Date().toLocaleDateString("en-CA")}
              value={checkOut}
              onChange={e => setCheckOut(e.target.value)}
            />
          </label>

          <button onClick={loadHotels}>
            <Search size={18}/> Search
          </button>
        </div>
      </section>

      <section className="results">
        <div className="section-head">
          <div>
            <p className="eyebrow">CURATED COLLECTION</p>
            <h2>Stays worth discovering</h2>
          </div>

          <span>{hotels.length} properties</span>
        </div>

        {message && <p className="message">{message}</p>}

        {loading && <p>Loading...</p>}

        <div className="hotel-grid">
          {hotels.map(h => (
            <article className="hotel-card" key={h.id}>
              <div className="hotel-image">
                <Hotel size={42}/>
                <span>LUXEKEY COLLECTION</span>
              </div>

              <div className="hotel-info">
                <p className="place">
                  <MapPin size={14}/> {h.location}
                </p>

                <h3>{h.name}</h3>
                <p>{h.description}</p>

                <div className="amenities">
                  {h.amenities?.join(" · ")}
                </div>

                <button
                  className="details"
                  onClick={() => viewRooms(h)}
                >
                  Explore rooms <ArrowRight size={16}/>
                </button>
              </div>
            </article>
          ))}
        </div>

        {selected && (
          <div className="room-panel">
            <button
              className="close"
              onClick={() => {
                setSelected(null);
                setBookingRoom(null);
              }}
            >
              ✕
            </button>

            <p className="eyebrow">ROOM AVAILABILITY</p>
            <h2>{selected.name}</h2>

            {message && <p className="message">{message}</p>}

            {rooms.length === 0 ? (
              <p>No rooms available for these dates.</p>
            ) : rooms.map(room => (
              <div className="room" key={room.id}>
                <div>
                  <h3>{room.roomType}</h3>
                  <p>Up to {room.capacity} guests</p>
                </div>

                <strong>₹{room.price} / night</strong>
                <strong>₹{room.price * nights} total</strong>

                <button onClick={() => {
                  setBookingRoom(room);
                  setMessage("");
                }}>
                  Book now
                </button>
              </div>
            ))}

            {bookingRoom && (
              <div className="booking-form">
                <p className="eyebrow">YOUR RESERVATION</p>
                <h3>Complete your booking</h3>

                <input
                  placeholder="Your full name"
                  value={customerName}
                  onChange={e => setCustomerName(e.target.value)}
                />

                <input
                  type="email"
                  placeholder="Email address"
                  value={customerEmail}
                  onChange={e => setCustomerEmail(e.target.value)}
                />

                <p>Room: {bookingRoom.roomType}</p>
                <p>Stay: {nights} nights</p>

                <h3>
                  Total: ₹{bookingRoom.price * nights}
                </h3>

                <button
                  onClick={confirmBooking}
                  disabled={loading}
                >
                  {loading ? "Processing..." : "Confirm booking"}
                </button>
              </div>
            )}
          </div>
        )}

        {confirmedBooking && (
          <div className="room-panel confirmation">
            <p className="eyebrow">RESERVATION SUCCESSFUL</p>

            <h2>Thank you for booking with LuxeKey!</h2>

            <p>Your reservation has been confirmed.</p>

            <div className="confirmation-details">
              <p>Booking Reference</p>
              <h2>{confirmedBooking.bookingReference}</h2>

              <p>Total Amount</p>
              <h2>₹{confirmedBooking.totalPrice}</h2>

              <p>Booking Status</p>
              <strong>{confirmedBooking.status}</strong>
            </div>

            <button
              onClick={() => setConfirmedBooking(null)}
              className="details"
            >
              Continue exploring
            </button>
          </div>
        )}
      </section>

      <footer>
        LuxeKey © 2026 · A more thoughtful way to stay.
      </footer>
    </main>
  );
}
