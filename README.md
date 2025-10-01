# Mini Doodle — A Meeting  Scheduling Service

This mini doodle is a small, high-performance meeting scheduling backend implemented with **Java**, **Spring Boot** and **PostgreSQL**. The service supports creating available time slots, booking meetings (which splits slots), marking parts of time as busy, querying availability, and persisting everything for correctness.

> This repository is intended to be runnable locally via `docker-compose`.

---

## Table of contents

* [Features](#features)
* [Quickstart (docker-compose)](#quickstart--docker-compose-)
* [API endpoints (overview + examples)](#api-endpoints--overview--examples-)
* [Data model & schema notes](#data-model--schema-notes)
* [Transactions & concurrency](#transactions--concurrency)
* [Testing](#testing)
* [Metrics & observability](#metrics--observability)
* [Design decisions & trade-offs](#design-decisions--trade-offs)
* [Next steps / TODOs](#next-steps--todos)
* [Developer's note](#developers-note)

---

## Features

* Create, update, delete user time slots (status: `FREE`, `BOOKED`, `BUSY`).
* Book a meeting that converts part of a `FREE` slot into a `BOOKED` slot.
* Mark arbitrary time slots as `BUSY`.
* Query free/busy slots for a user across a timeframe.
* Persisted to Postgres with constraints that prevent overlapping slots for the same user.
* Runnable locally with Docker Compose (app + postgres).
* OpenAPI docs (Swagger) and basic Micrometer metrics endpoints (Prometheus compatible).

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

Please Note: The container startup for the first time will take some time as we will pre-populate some data in the database.

---

## API endpoints (overview + examples)

The service exposes JSON REST endpoints under `/api/v1`.

**User**

* `GET /users` — get all users

Please note: CRUD operations on the users is not in scope of this application. We are assuming that the users already exists. Some users will be pre-populated along with some dummy time slots and meeting data.

**Time slots**

* `POST /slots/users/{userId}` — create slot (single)
* `GET  /slots/{slotId}` — list slots by Id
* `GET  /slots/users/{userId}?from=...&to=...&status=...&page=...&size=...` — list slots by the specified user and filters applied
* `GET  /slots/users?from=...&to=...&userId=...&userId=...` — list slots of multiple users with filtered range
* `PUT  /slots/{slotId}` — modify slot
* `DELETE /slots/{slotId}` — delete slot
* `GET /slots/users/availability?userId=...&userId=...&userId=...&from=...&to=...` — Gets availability of the specified users for the time range. Should be used before scheduling meetings to avoid conflicts.

**Meetings**

* `GET /meetings/{id}` — get meeting details
* `GET /meetings/users?userid=...&userId=...&userId=...&from=...&to=...` — get meeting details for multiple users for the specified time range
* `POST /meetings` — creates a meeting with participants, creator, start, end, title and description. Modifies the availability of the users time slots accordingly.
* `DELETE /meetings/{id}` — deletes the meeting. Updates the availability of the participants.

If successful, the API will return `201 Created` with meeting details and the affected `slotId`. If the slot was not `FREE` or was already booked, the API returns `409 Conflict`.

---

### Implementation notes:

* All status updates on meeting schedule/cancellation logic is performed inside transactional service methods to ensure atomic updates.

* A `status` field marks the type of slot: `FREE`, `BOOKED`, `BUSY`.

The DB exclusion constraint prevents creating overlapping slots for the same user at the storage level.

---

## Data model & schema notes

### Main tables:

* `users (id, email, name)`

* `time_slots (id, userId, startTime, endTime, status)`

* `meetings (id, title, description, start, end, participants)`

---

## Transactions & concurrency

* Concurrent transactions, which could be achieved via optimistic locking, is not covered in this version.

* The service returns `409 Conflict` when booking fails due to competing bookings.

---

## Testing

* Unit tests: service-level unit tests using Mockito.


Run tests:
````BASH
./mvnw test
````

---

## Metrics & observability

* Micrometer exposes metrics at `/actuator/prometheus` (if enabled).

* Key metrics: `meetings_booked_total`, `meetings_deleted_total`, `meetings_conflict_total`, `timeslots_created_total`, `timeslots_conflicts_total`, `timeslots_updated_total`, `timeslots_deleted_total`.

* OpenAPI/Swagger available at `http://localhost:8080/swagger-ui/index.html`.

---

## Design decisions & trade-offs

* **Slots as fixed intervals vs. event-sourced boundary**: I chose to use fixed 30-min intervals to create time slots for simplicity.

* **DB as single source of truth**: transactions are used for correctness; cache (Redis) is optional to speed reads.

* **Scale**: designed for hundreds of users and thousands of slots; for larger scale we would partition or move to specialized time-indexed stores.

---

## Next steps / TODOs

* Configurable time slots with dynamic range for scheduling.

* Preventing creation of meetings/slots in the past.

* Preventing creation of duplicate slots.

* Preventing creation of overlapping slots (2025-10-18T09:00:00 - 2025-10-18T09:30:00 already exists and user is trying to create a time slot 2025-10-18T09:15:00 to 2025-10-18T09:45:00)

* Complete implementation of recurrence rules and bulk-generation of recurring slots.

* Add push/email notifications when meetings are created or changed (event queue + worker).

* Implement participant RSVP states and meeting updates / reschedules.

* Add load tests and CI pipeline.

---

## Developer's note

The current implementations gives freedom to the user to play with the APIs. These APIs can be orchestrated to perform actions more specific and limited to the meeting booking and scheduling, something similar to Calendly orTeams meetings.

Another way to implement this problem statement could be having aggregated APIs. A view for one or group of users can aggregate the APIs like `/slots/users/availability`, `/slots/users/{userId}` and `/meetings/users`. Booking a meeting can aggregate APIs `/slots/users/availabilty` and `/meetings`. I can provide with high-level diagrams or implementations if needed.
