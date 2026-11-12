# LuxeKey --- Hotel Booking Portal

A hotel discovery and room-booking web application built as a Java
Developer Intern assessment project. LuxeKey lets users browse hotels,
search by destination, check room availability for travel dates,
calculate the stay price, and create a booking with a unique reference.

## Project status

The core demo flow has been implemented and tested locally:

-   Five sample hotels are inserted into MySQL by a startup data seeder.
-   Hotel listing, hotel details, location search, and room-list APIs
    are available.
-   Date-based room availability is checked against existing confirmed
    bookings.
-   Booking price is calculated by the backend using room price × number
    of nights.
-   A successful booking receives a unique reference such as
    `LK-XXXXXXXX`.
-   Booking lookup and cancellation endpoints are implemented in the
    backend.
-   The React interface displays a booking confirmation with reference,
    total amount, and status.

**Current implementation limitations:** this is an assessment prototype,
not a production booking platform. Customer registration/login is not
implemented. The frontend currently uses a demo customer ID (`1`), so
the corresponding `CUSTOMER` record must exist in the database. The name
and email entered in the booking form are currently UI inputs and are
not used to create or authenticate a customer. Payment integration,
admin/manager dashboards, real hotel inventory, and deployment are not
included.

## Technology stack

  Layer         Technology
  ------------- --------------------------------------
  Frontend      React, Vite, Axios, Lucide React
  Backend       Java 21, Spring Boot, Spring Web MVC
  Persistence   Spring Data JPA, Hibernate
  Database      MySQL 8
  Build         Maven
  Styling       CSS

## Project structure

``` text
LuxeKey/
├── backend/
│   ├── src/main/java/com/luxekey/backend/
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── DataSeeder.java
│   │   └── BackendApplication.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── main.jsx
│   └── package.json
├── database/
├── screenshots/
├── README.md
└── DESIGN.md
```

## Prerequisites

-   Java 21
-   MySQL Server 8.x
-   Node.js and npm
-   Maven wrapper (included in the backend project)

## Database setup

Create the database in MySQL:

``` sql
CREATE DATABASE luxekey;
```

Configure `backend/src/main/resources/application.properties`:

``` properties
spring.application.name=LuxeKey
spring.datasource.url=jdbc:mysql://localhost:3306/luxekey
spring.datasource.username=root
spring.datasource.password=YOUR_LOCAL_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
server.port=8080
```

Replace the password with your local MySQL password. Do not commit real
credentials. For a shared/public repository, use environment variables
or an untracked local configuration file.

The `DataSeeder` inserts five sample hotels and one Deluxe Room per
hotel when the hotel table is empty. The sample records are fictional
development data.

For the demo booking flow, create a customer if one does not already
exist:

``` sql
USE luxekey;

INSERT INTO users (name, email, password, role)
VALUES ('Demo Customer', 'customer@example.com', 'demo-only', 'CUSTOMER');
```

The current frontend is configured with `CUSTOMER_ID = 1`. If the
inserted customer has a different ID, update that constant in
`frontend/src/App.jsx`. The password field is not used for
authentication in this prototype; do not use real credentials.

## Run the application

### 1. Start the backend

Open a terminal:

``` powershell
cd D:\LuxeKey\backend
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

Backend base URL: `http://localhost:8080`

### 2. Start the frontend

Open a second terminal:

``` powershell
cd D:\LuxeKey\frontend
npm install
npm install axios lucide-react
npm run dev
```

Frontend URL: `http://localhost:5173`

Keep both terminals running while using the application.

## API endpoints

Base URL: `http://localhost:8080/api`

  --------------------------------------------------------------------------------------------------------------------------
  Method                  Endpoint                                                                   Purpose
  ----------------------- -------------------------------------------------------------------------- -----------------------
  GET                     `/hotels`                                                                  List approved hotels

  GET                     `/hotels/{id}`                                                             Get an approved hotel's
                                                                                                     details

  GET                     `/hotels/search?location=Mumbai`                                           Search approved hotels
                                                                                                     by location

  GET                     `/hotels/{id}/rooms`                                                       List rooms belonging to
                                                                                                     a hotel

  GET                     `/hotels/{id}/availability?checkIn=2026-12-08&checkOut=2026-12-09`         List rooms available
                                                                                                     for the requested dates

  POST                    `/bookings?roomId=1&customerId=1&checkIn=2026-12-08&checkOut=2026-12-09`   Create a booking

  GET                     `/bookings/{reference}?email=customer@example.com`                         Retrieve a booking by
                                                                                                     reference and customer
                                                                                                     email

  PUT                     `/bookings/{reference}/cancel?email=customer@example.com`                  Cancel a booking
  --------------------------------------------------------------------------------------------------------------------------

Dates use `YYYY-MM-DD`. The booking POST currently accepts query
parameters, not a JSON request body.

## Booking rules

-   Check-out must be later than check-in.
-   Check-in cannot be in the past.
-   The backend calculates the number of nights and total price; it does
    not trust a frontend-supplied total.
-   Only approved hotels can be booked.
-   A room cannot have two confirmed bookings whose date ranges overlap.
-   A cancelled booking no longer blocks those dates.
-   The backend locks the room row during booking creation to serialize
    competing booking attempts for that room.

Two stays overlap when:

``` text
existing.checkIn < requested.checkOut
AND existing.checkOut > requested.checkIn
```

This permits back-to-back stays: one booking can check out on the same
date another checks in.

## Manual verification

1.  Start MySQL, backend, and frontend.
2.  Open `http://localhost:5173`.
3.  Search for a destination such as Mumbai.
4.  Select check-in and check-out dates.
5.  Open a hotel's rooms and review the displayed price.
6.  Enter the demo customer details and confirm the booking.
7.  Verify the generated reference and `CONFIRMED` status in the UI.
8.  Inspect the `booking` table in MySQL.
9.  Request availability for the same overlapping dates; the booked room
    should no longer be returned.
10. Try a non-overlapping date range; the room should be available
    again.

Useful SQL:

``` sql
USE luxekey;

SELECT id, booking_reference, check_in, check_out,
       total_price, status, room_id, customer_id
FROM booking;
```

## Screenshots

Add assessment screenshots to the `screenshots/` folder. Suggested
filenames:

-   `01-hotel-search.png`
-   `02-hotel-details.png`
-   `03-room-availability.png`
-   `04-booking-form.png`
-   `05-booking-confirmation.png`
-   `06-mysql-booking-record.png`

## Known limitations and next improvements

-   Implement proper customer registration and authentication; remove
    the hard-coded customer ID.
-   Persist and validate the booking form's customer name and email
    against the customer record.
-   Add structured validation and consistent API error responses instead
    of generic runtime exceptions.
-   Use `BigDecimal` for monetary values rather than `double`.
-   Add automated tests for price calculation, date validation,
    overlapping stays, cancellation, and concurrent booking attempts.
-   Add a dedicated booking history/lookup and cancellation interface in
    React.
-   Add a separate, clearly scoped surprise feature if time permits.
-   Add hotel imagery and richer room inventory; current seeded data is
    illustrative.
-   Move secrets to environment variables and use production-safe
    database settings.

## AI assistance disclosure

AI assistance was used to help plan the project, generate and revise
parts of the code, troubleshoot build/runtime issues, and draft project
documentation. The implementation was run and manually checked locally,
including hotel API responses, room availability, and successful booking
persistence in MySQL. The project owner should review and understand the
submitted code before the assessment.
