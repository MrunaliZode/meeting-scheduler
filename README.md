# Mini Doodle — A Meeting  Scheduling Service

This mini doodle is a small, high-performance meeting scheduling backend implemented with **Java**, **Spring Boot** and **PostgreSQL**. The service supports creating available time slots, booking meetings (which splits slots), marking parts of time as busy, querying availability, and persisting everything for correctness.

> This repository is intended to be runnable locally via `docker-compose`.

---

## Table of contents

* [Features](#features)
* [Key concepts & correctness guarantees](#key-concepts--correctness-guarantees)
* [Quickstart (docker-compose)](#quickstart-docker-compose)
* [API endpoints (overview + examples)](#api-endpoints-overview--examples)
* [Time slot behavior (split & merge rules)](#time-slot-behavior-split--merge-rules)
* [Data model & schema notes](#data-model--schema-notes)
* [Transactions & concurrency](#transactions--concurrency)
* [Testing and running integration tests](#testing-and-running-integration-tests)
* [Metrics & observability](#metrics--observability)
* [Design decisions & trade-offs](#design-decisions--trade-offs)
* [Next steps / TODOs](#next-steps--todos)

---

## Features

* Create, update, delete user time slots (status: `FREE`, `BOOKED`, `BUSY`, `CANCELLED`).
* Book a meeting that converts part of a `FREE` slot into a `BOOKED` slot (splits original slot as needed).
* Mark arbitrary ranges as `BUSY` (also splits `FREE` slots).
* Query free/busy slots for a user across a timeframe.
* Persisted to Postgres with constraints that prevent overlapping slots for the same user.
* Runnable locally with Docker Compose (app + postgres).
* OpenAPI docs (Swagger) and basic Micrometer metrics endpoints (Prometheus compatible).

---

## Key concepts & correctness guarantees

* **No overlapping slots**: the database uses a GiST exclusion constraint on a stored `tstzrange` to prevent overlapping time ranges for the same user.
* **Booking correctness**: booking is implemented inside a transaction that locks the affected slot using `SELECT ... FOR UPDATE` (or optimistic locking with `@Version` depending on configuration). This prevents race conditions and double-booking.
* **Slot splitting & merging**: when a booking or busy-block is created inside a larger `FREE` slot, the slot is split into up to three pieces. When a booked/busy interval is released, adjacent `FREE` slots are merged to reduce fragmentation.

---

## Quickstart (docker-compose)

Prerequisites:

* Docker & docker-compose installed
* Java (if you want to build locally without Docker)

Run the service (build will use included Dockerfile):

```bash
# from repository root
docker-compose up --build
```

This brings up:

* `app` — Spring Boot application on `http://localhost:8080`
* `db` — Postgres on `localhost:5432` (credentials are in `docker-compose.yml`)

OpenAPI UI (Swagger) will be available at: `http://localhost:8080/swagger-ui.html` when the app is running.

---

## API endpoints (overview + examples)

The service exposes JSON REST endpoints under `/api/v1`.

**User**

* `POST /api/v1/users` — create user

**Time slots**

* `POST /api/v1/users/{userId}/slots` — create slot (single or batch)
* `GET  /api/v1/users/{userId}/slots?from=...&to=...&status=FREE` — list slots by range and status
* `PUT  /api/v1/users/{userId}/slots/{slotId}` — modify slot
* `DELETE /api/v1/users/{userId}/slots/{slotId}` — delete/cancel slot
* `POST /api/v1/users/{userId}/slots/{slotId}/book` — book a `FREE` slot (creates Meeting)
* `POST /api/v1/users/{userId}/slots/block` — mark an arbitrary range as `BUSY` (splits underlying free slots)

**Meetings**

* `GET /api/v1/meetings/{meetingId}` — get meeting details

### Example: Create a day-long free slot

```bash
curl -X POST http://localhost:8080/api/v1/users/{userId}/slots \
  -H 'Content-Type: application/json' \
  -d '{ "start": "2025-09-29T09:00:00Z", "end": "2025-09-29T17:00:00Z" }'
```

### Example: Book a meeting in a slot

```bash
curl -X POST http://localhost:8080/api/v1/users/{userId}/slots/{slotId}/book \
  -H 'Content-Type: application/json' \
  -d '{ "title": "Interview", "organizerId": "uuid-1", "participants": ["uuid-2"] }'
```

If successful, the API will return `201 Created` with meeting details and the affected `slotId`. If the slot was not `FREE` or was already booked, the API returns `409 Conflict`.

---

## Time slot behavior (split & merge rules)

### Behaviour example:

1. Create: `09:00–17:00` (FREE)

2. Book `10:30–11:30` → splits into:

* `09:00–10:30` FREE

* `10:30–11:30` BOOKED (Meeting)

* `11:30–17:00` FREE

3. Block `14:00–15:00` as BUSY → splits `11:30–17:00` into:

* `11:30–14:00` FREE

* `14:00–15:00` BUSY

* `15:00–17:00` FREE

4. Cancel `10:30–11:30` booking → the `BOOKED` slot becomes `FREE` and adjacent FREE slots are merged if contiguous (result: `09:00–11:30` FREE and `11:30–14:00` FREE may then be merged to `09:00–14:00` depending on contiguous boundaries and business rules).

### Implementation notes:

* All split/merge logic is performed inside transactional service methods to ensure atomic updates.

* A `status` field marks the type of slot: `FREE`, `BOOKED`, `BUSY`, `CANCELLED`.

The DB exclusion constraint prevents creating overlapping slots for the same user at the storage level.

---

## Data model & schema notes

### Main tables:

* `users (id, email, display_name, created_at)`

* `time_slots (id, user_id, start_ts, end_ts, ts_range, status, created_at, updated_at, version)`

* `meetings (id, slot_id, title, description, organizer_id, participants_json, created_at)`

### Important DB-level constraints:

* `tstzrange(start_ts, end_ts, '[)')` stored column `ts_range`.

* `EXCLUDE USING GIST (user_id WITH =, ts_range WITH &&)` to ensure no overlapping ranges per user.

* Optional JPA `@Version` column for optimistic locking.

---

## Transactions & concurrency

* Booking or blocking operations use a transaction that either `SELECT ... FOR UPDATE` on an identified slot (pessimistic) or rely on optimistic locking (JPA `@Version`) with retries.

* The service returns `409 Conflict` when booking fails due to concurrent competing bookings.

* Integration tests verify concurrent booking attempts and expected conflict behavior.

---

## Testing and running integration tests

* Unit tests: service-level unit tests using Mockito.

* Integration tests: use Testcontainers (Postgres) to run integration tests that verify exclusion constraints and split/merge behavior.

Run tests:
````BASH
./mvnw test
# or
./gradlew test
````

---

## Metrics & observability

* Micrometer exposes metrics at `/actuator/prometheus` (if enabled).

* Key metrics: `bookings_total`, `bookings_conflict_total`, `slot_creations_total`, DB transaction latency.

* OpenAPI/Swagger available at `/swagger-ui.html`.

---

## Design decisions & trade-offs

* **Slots as persisted intervals vs. event-sourced boundary**: We chose persisted, non-overlapping intervals in Postgres for simplicity, correctness, and to leverage DB constraints.

* **Splitting slots**: leads to fragmentation but keeps queries for availability simple and fast (select `status=FREE`). Merging on release reduces fragmentation.

* **DB as single source of truth**: transactions are used for correctness; cache (Redis) is optional to speed reads.

* **Scale**: designed for hundreds of users and thousands of slots; for larger scale we would partition or move to specialized time-indexed stores.

---

## Next steps / TODOs

* Complete implementation of recurrence rules and bulk-generation of recurring slots.

* Add push/email notifications when meetings are created or changed (event queue + worker).

* Implement participant RSVP states and meeting updates / reschedules.

* Add load tests and CI pipeline.
