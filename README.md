# HiveWatch Lite

HiveWatch Lite is a full-stack beehive monitoring prototype built with a Spring Boot REST API and a React + TypeScript front end.

It was developed for the **CT5221 Full Stack App Development** module using a realistic beekeeping domain and manages two related entities:

- `Hive`
- `TemperatureReading`

The application supports CRUD operations for both entities and includes a relationship workflow that allows a temperature reading to be reassigned to a different hive.

---

## Why this project matters

This repository is intended to show practical software development skills across:

- Java and Spring Boot
- React and TypeScript
- REST API design
- layered architecture
- DTO-based request and response handling
- relational modelling with JPA
- front-end and back-end integration
- service-layer validation beyond thin CRUD
- automated back-end testing

---

## Running the project locally

This section is written as a step-by-step guide so the project is easy to run for a lecturer, reviewer, or employer.

### Prerequisites

You will need:

- JDK 25 installed
- Node.js and npm installed
- the Gradle wrapper already included in the repository

### 1. Start the back end

Open a terminal in the **repository root** and run:

**Windows**
```bash
gradlew.bat bootRun
```

**macOS or Linux**
```bash
./gradlew bootRun
```

This starts the Spring Boot back end on:

```text
http://localhost:8080
```

The back end uses an in-memory H2 database for local development, so no separate database setup is needed for a basic run.

### 2. Check that the back end is running

Open the following in your browser:

```text
http://localhost:8080
```

You should see a small JSON status response confirming that the **HiveWatch Lite API** is running.

You can also test the API directly at:

```text
http://localhost:8080/api/hives
http://localhost:8080/api/readings
```

### 3. Start the front end

Open a **second terminal** and move into the `frontend` folder:

```bash
cd frontend
npm install
npm run dev
```

The React front end usually runs on:

```text
http://localhost:5173
```

> If `npm` says it cannot find `package.json`, make sure you are inside the `frontend` folder rather than the repository root.

### 4. Open the application in the browser

Once both parts are running, open:

```text
http://localhost:5173
```

Use the following addresses during local development:

- `http://localhost:5173` for the React front end
- `http://localhost:8080` for the Spring Boot API status endpoint
- `http://localhost:8080/api/hives` for the hive API
- `http://localhost:8080/api/readings` for the temperature reading API

### 5. Optional: open the H2 console

The project also exposes the H2 database console locally at:

```text
http://localhost:8080/h2-console
```

Use the following settings:

- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: blank

> These settings are for local development only.

### 6. Run the automated tests

From the repository root, run:

**Windows**
```bash
gradlew.bat cleanTest test
```

**macOS or Linux**
```bash
./gradlew cleanTest test
```

Then open the generated Gradle HTML test report:

```text
build/reports/tests/test/index.html
```

For local code coverage analysis, open the JaCoCo report at:

```text
build/reports/jacoco/test/html/index.html
```

---

## Architecture

This project uses a layered full-stack structure.

The React front end provides the user interface, calls the Spring Boot REST API, and displays data returned from the back end. The back end uses controllers to expose endpoints, services to apply business logic, repositories to handle persistence, DTOs to shape payloads, and entities to represent the domain model.

```mermaid
flowchart TD
    A["User in Browser"] --> B["React + TypeScript Front End\nVite dev server"]
    B --> C["HiveWatch API Client"]
    C --> D["HiveController"]
    C --> E["TemperatureReadingController"]
    D --> F["HiveService / HiveServiceImpl"]
    E --> G["TemperatureReadingService / TemperatureReadingServiceImpl"]
    F --> H["HiveRepository"]
    G --> I["TemperatureReadingRepository"]
    F --> J["HiveDTO / WriteHiveDTO"]
    G --> K["TemperatureReadingDTO / WriteTemperatureReadingDTO"]
    H --> L[("H2 / MariaDB")]
    I --> L
    L --> M["Hive"]
    L --> N["TemperatureReading"]
```

---

## Key features

### Back end

- Spring Boot REST API
- layered structure using controller, service, repository, entity, and DTO classes
- H2 in-memory database for local development
- MariaDB example configuration included for optional local or future use

### Front end

- React + TypeScript UI
- forms and tables for hives and readings
- create, edit, delete, and filter workflows
- relationship reassignment through the UI
- consistent displayed date formatting in the UI

### Domain behaviour

- one hive can have many temperature readings
- each temperature reading belongs to one hive
- relationship reassignment is supported
- search, filtering, aggregation, and batch update behaviour are included

---

## Application layers

### Front end

- React
- TypeScript
- Vite
- API client layer for HTTP requests
- reusable UI components

### Back end

- entities: `Hive`, `TemperatureReading`
- DTOs: `HiveDTO`, `TemperatureReadingDTO`, `WriteHiveDTO`, `WriteTemperatureReadingDTO`
- repositories: `HiveRepository`, `TemperatureReadingRepository`
- services: `HiveService`, `HiveServiceImpl`, `TemperatureReadingService`, `TemperatureReadingServiceImpl`
- controllers: `HiveController`, `TemperatureReadingController`, `HomeController`

---

## Business rules

### Hive rules

- hive name is required
- hive location is required
- hive name must be 2 to 50 characters
- hive location must be 2 to 80 characters
- hive name must be unique
- a hive cannot be deleted if temperature readings still exist for it

### Temperature reading rules

- `hiveId` is required when creating or updating a reading
- temperature is required
- `recordedAt` is required when recording or updating a reading
- temperature must be between `-9.0` and `46.5` degrees Celsius
- `recordedAt` cannot be in the future
- duplicate timestamps for the same hive are blocked
- a reading cannot be reassigned to another hive if that would create a timestamp conflict
- batch offset updates are limited to values between `-20.0` and `+20.0`

---

## API capabilities

### Hive endpoints

Base route: `/api/hives`

Implemented operations:

- create a hive
- get all hives
- get hive by id
- update hive
- delete hive
- find hive by exact name
- search hive names by fragment
- search hive locations by fragment
- combined search by name and or location
- rename hive
- relocate hive

### Temperature reading endpoints

Base route: `/api/readings`

Implemented operations:

- create a reading
- get all readings
- get reading by id
- update reading
- delete reading
- list readings for a hive
- get latest reading for a hive
- get readings for a hive between two timestamps
- count readings for a hive
- calculate average temperature for the last N minutes
- assign a reading to a different hive
- apply a temperature offset across all readings for a hive

---

## Representative API validation

In addition to the automated JUnit suite, the REST API was validated manually in Postman against the local Spring Boot back end at `http://localhost:8080`. These representative checks were used to confirm successful retrieval, successful creation, and exception handling through real HTTP requests.

### Hive validation

- `GET /api/hives` returns the current hive list (`200 OK`)
- `POST /api/hives` creates a new hive (`201 Created`)
- `GET /api/hives/1` returns a specific hive by id (`200 OK`)

### Temperature reading validation

- `POST /api/readings` creates a new reading for an existing hive (`201 Created`)
- `GET /api/readings/1` returns a specific reading by id (`200 OK`)

### Exception handling proof

- `GET /api/readings/99999` returns `404 Not Found` for an unknown reading id

These manual checks complement the automated repository, service, and controller tests by showing the API working end to end through real HTTP requests and responses.

---

## Back-end testing

A layered JUnit testing suite was added to the back end to verify repository behaviour, service-layer business rules, and controller-level HTTP handling.

### Test stack

- JUnit 5
- Mockito
- `@DataJpaTest` with H2 for repository tests
- `@WebMvcTest` with `MockMvc` for controller tests
- Gradle test execution and HTML test reporting
- JaCoCo coverage reporting for local coverage analysis

### Test classes

The core layered suite spans six test classes:

- `HiveRepositoryTest`
- `TemperatureReadingRepositoryTest`
- `HiveServiceImplTest`
- `TemperatureReadingServiceImplTest`
- `HiveControllerTest`
- `TemperatureReadingControllerTest`

The layered suite was verified successfully across repository, service, and controller levels, and selected parameterized service tests were then added to strengthen validation coverage further.

### What is being verified

The test suite verifies:

- repository queries, ordering, averages, and batch updates
- service-layer business rules such as duplicate hive prevention, blocked delete behaviour, required timestamps, timestamp conflicts, and validation of numeric boundaries
- controller request and response handling, including expected `201`, `200`, `400`, and `409` outcomes

JaCoCo coverage reporting was added to complement the passing JUnit suite and provide a local view of which parts of the back-end code are currently exercised by automated tests.

### Boundary-focused testing

Additional boundary-focused service tests were added for selected validation rules, including:

- temperature boundaries
- average window minute boundaries
- offset delta boundaries

### Test traceability

A lightweight [requirements-to-test traceability document](docs/test-traceability.md) is included in the repository.

The screenshot below shows the final Gradle HTML test summary for the back-end suite.

![Gradle test summary](docs/images/gradle-test-summary.jpg)

---

## Evidence

### API proof in Postman

![Postman API proof](docs/images/postman-get-all-hives.jpg)

### Persistence proof in H2

![H2 seeded data proof](docs/images/h2-seeded-data.jpg)

### React front end

#### Hives screen

![Hives screen](docs/images/react-hives-screen.jpg)

#### Temperature readings screen

![Temperature readings screen](docs/images/react-readings-screen.jpg)

#### Relationship update

![Assign Hive dialog](docs/images/react-assign-hive-dialog.jpg)

---

## Technology stack

### Back end

- Java 25 toolchain as configured in Gradle
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 Database
- MariaDB driver
- Gradle

### Front end

- React
- TypeScript
- Vite

### Development and testing

- JUnit 5
- Mockito
- Spring `MockMvc`
- H2 in-memory database
- Gradle test execution and HTML test reporting
- JaCoCo coverage reporting for local coverage analysis
- Postman
- browser-based UI testing

---

## What this project shows

This repository demonstrates:

- a realistic domain rather than a generic tutorial app
- a clean layered back-end structure
- RESTful API design with both CRUD and domain-specific operations
- DTO usage for clearer request and response handling
- service-layer validation and business logic
- relational modelling with a one-to-many association
- search, filtering, aggregation, and batch update behaviour
- a working React front end connected to a Spring Boot API
- relationship editing through the UI
- local development workflow using H2, Postman, and React
- a layered automated back-end testing approach across repository, service, and controller levels
- boundary-focused automated testing for selected validation rules
- traceability from selected business rules to automated tests

---

## Possible future enhancements

Potential future improvements include:

- optionally add SonarQube quality scanning and a CI quality gate
- expand integration-style API tests for key end-to-end flows
- extend automated testing further as the domain model grows

---

## Repository structure

```text
.
├── docs/
│   ├── images/
│   └── test-traceability.md
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   └── utils/
│   ├── .env
│   ├── package-lock.json
│   ├── package.json
│   └── vite.config.ts
├── gradle/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/com/hivewatch/hivewatchlite/
│   │   │   ├── HivewatchliteApplication.java
│   │   │   ├── StartupRunner.java
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repo/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.example.properties
│   │       └── application.properties
│   └── test/
│       └── java/com/hivewatch/hivewatchlite/
│           ├── controller/
│           ├── repo/
│           └── service/
├── .gitattributes
├── .gitignore
├── HELP.md
├── README.md
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```