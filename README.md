# Vet1177 – Veterinärjournalsystem

Ett webbaserat journalhanteringssystem för veterinärkliniker. Husdjursägare kan skapa ärenden, veterinärer kan hantera journaler och bifoga filer, och administratörer har full tillgång till systemet.

---

## Innehåll

- [Tech-stack](#tech-stack)
- [Kom igång](#kom-igång)
- [Miljövariabler](#miljövariabler)
- [Projektstruktur](#projektstruktur)
- [API-endpoints](#api-endpoints)
- [Roller och behörigheter](#roller-och-behörigheter)
- [Säkerhet](#säkerhet)
- [Databasmigration (Flyway)](#databasmigration-flyway)
- [Fillagring (MinIO/S3)](#fillagring-minios3)
- [Testning](#testning)
- [CI/CD](#cicd)
- [Felhantering](#felhantering)

---

## Tech-stack

### Backend

| Kategori        | Teknologi                          |
|-----------------|------------------------------------|
| Språk           | Java 24                            |
| Ramverk         | Spring Boot 4.0.4                  |
| Databas         | PostgreSQL 15                      |
| ORM             | Spring Data JPA / Hibernate        |
| Migration       | Flyway (versionerade migrationer)  |
| Säkerhet        | Spring Security 6                  |
| Fillagring      | MinIO (S3-kompatibel)              |
| Loggning        | SLF4J                              |
| Byggsystem      | Maven                              |
| Containerisering| Docker / Docker Compose            |

### Frontend

| Kategori        | Teknologi                          |
|-----------------|------------------------------------|
| Ramverk         | React 19                           |
| Byggverktyg     | Vite 8                             |
| Styling         | Tailwind CSS 3                     |
| HTTP-klient     | Axios                              |

---

## Kom igång

### Förutsättningar

- Java 24
- Maven
- Docker & Docker Compose
- Node.js (för frontend)

### Installation

**1. Klona repot**
```bash
git clone <repo-url>
cd project-backend-org-random-coders
```

**2. Skapa miljöfil**
```bash
cp .env.example .env
```
Fyll i värdena i `.env` (se [Miljövariabler](#miljövariabler)).

**3. Starta infrastruktur (PostgreSQL + MinIO)**
```bash
docker compose up -d
```

**4. Starta backend**
```bash
./mvnw spring-boot:run
```

Backenden startar på `http://localhost:8080`.

**5. Starta frontend**
```bash
cd frontend
npm install
npm run dev
```

Frontenden startar på `http://localhost:5173`.

> MinIO-konsolen nås på `http://localhost:9001` (använd dina `MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD`).

---

## Miljövariabler

| Variabel           | Beskrivning                      | Exempel                                    |
|--------------------|----------------------------------|--------------------------------------------|
| `DB_URL`           | PostgreSQL JDBC URL              | `jdbc:postgresql://localhost:5432/vet1177` |
| `DB_USERNAME`      | Databasanvändare                 | `postgres`                                 |
| `DB_PASSWORD`      | Databaslösenord                  | –                                          |
| `DB_NAME`          | Databasnamn                      | `vet1177`                                  |
| `S3_ENDPOINT`      | MinIO/S3 URL                     | `http://localhost:9000`                    |
| `S3_ACCESS_KEY`    | S3 access key                    | –                                          |
| `S3_SECRET_KEY`    | S3 secret key                    | –                                          |
| `S3_BUCKET`        | Bucket-namn för bilagor          | `vet1177-attachments`                      |
| `S3_REGION`        | S3-region                        | `eu-north-1`                               |
| `MINIO_ROOT_USER`  | MinIO admin-användare            | –                                          |
| `MINIO_ROOT_PASSWORD` | MinIO admin-lösenord          | –                                          |

---

## Projektstruktur

```
src/main/java/org/example/vet1177/
├── config/              # Spring-konfiguration (MinIO/S3)
├── controller/          # REST-controllers
├── dto/
│   ├── request/         # Inkommande request-objekt (med validering)
│   └── response/        # Utgående response-objekt
├── entities/            # JPA-entiteter
├── exception/           # Anpassade undantag och global felhanterare
├── policy/              # Auktoriseringslogik per roll
├── repository/          # Spring Data JPA-repositories
├── security/            # Spring Security-konfiguration + auth
│   └── auth/dto/        # Auth-relaterade DTOs
└── services/            # Affärslogik

frontend/src/
├── components/          # Återanvändbara UI-komponenter
├── pages/               # Sidkomponenter per roll (Owner, Vet, Admin)
├── services/            # API-anrop (axios)
└── utils/               # Hjälpfunktioner
```

### Entiteter

| Entitet         | Beskrivning                                              |
|-----------------|----------------------------------------------------------|
| `User`          | Användare med roll: `OWNER`, `VET`, `ADMIN`              |
| `Vet`           | Utökar User med licensnummer och specialisering          |
| `Clinic`        | Veterinärklinik                                          |
| `Pet`           | Husdjur knutet till en ägare                             |
| `MedicalRecord` | Ärende/journal – kärnan i systemet                       |
| `Comment`       | Kommentar på ett ärende                                  |
| `Attachment`    | Bifogad fil lagrad i MinIO/S3                            |
| `ActivityLog`   | Revisionslogg över händelser på ett ärende               |

### Ärendestatus

Status är en enum (`RecordStatus`):

- `OPEN` — nytt ärende, ingen handläggare tilldelad
- `IN_PROGRESS` — handläggning pågår (sätts automatiskt vid `assignVet`)
- `AWAITING_INFO` — väntar på komplettering från ägaren
- `CLOSED` — avslutat (slutstatus, kräver slutnotering vid stängning)

VET kan fritt växla mellan `OPEN`, `IN_PROGRESS` och `AWAITING_INFO` via `PUT /{id}/status`. Stängning sker bara via dedikerad `PUT /{id}/close`.

---

## API-endpoints

Bas-URL: `/api`

### Autentisering – `/api/auth`

| Metod | Sökväg      | Beskrivning       |
|-------|-------------|-------------------|
| POST  | `/login`    | Logga in (JWT)    |
| POST  | `/register` | Registrera ägare  |

### Journaler – `/api/medical-records`

| Metod  | Sökväg                               | Beskrivning                        |
|--------|--------------------------------------|------------------------------------|
| POST   | `/`                                  | Skapa nytt ärende                  |
| GET    | `/{id}`                              | Hämta ärende via ID                |
| GET    | `/my-records`                        | Inloggad ägares ärenden            |
| GET    | `/my-assigned`                       | Inloggad veterinärs tilldelade     |
| GET    | `/owner/{ownerId}`                   | Ärenden för en ägare               |
| GET    | `/pet/{petId}`                       | Ärenden för ett husdjur            |
| GET    | `/clinic/{clinicId}`                 | Ärenden på en klinik               |
| GET    | `/clinic/{clinicId}/status/{status}` | Filtrera på klinik och status      |
| PUT    | `/{id}`                              | Uppdatera ärende                   |
| PUT    | `/{id}/assign-vet`                   | Tilldela veterinär                 |
| PUT    | `/{id}/unassign-vet`                 | Tilldelad VET släpper ärendet      |
| PUT    | `/{id}/status`                       | Uppdatera status                   |
| PUT    | `/{id}/close`                        | Stäng ärende                       |

### Övriga endpoints

| Prefix               | Beskrivning                          |
|----------------------|--------------------------------------|
| `/api/users`         | CRUD för användare (admin)           |
| `/api/clinics`       | CRUD för kliniker                    |
| `/api/vets`          | Hämta, skapa och uppdatera veterinärer |
| `/api/pets`          | Husdjurshantering                    |
| `/api/comments`      | Kommentarer på ärenden               |
| `/api/attachments`   | Bilagor (uppladdning, nedladdning)   |
| `/api/activity-logs` | Revisionslogg                        |

---

## Roller och behörigheter

Auktoriseringen sker i två lager:

**Lager 1 — URL-nivå** i `SecurityConfig` (`requestMatchers(...).hasAnyRole(...)`) — grovmaskigt rollfilter.
**Lager 2 — Policy-klasser** — finkornig ägarskap, kliniktillhörighet och statuschecks. Kallas från service eller controller innan mutationer.

| Policy-klass | Ansvar |
|---|---|
| `MedicalRecordPolicy` | canCreate / canView / canUpdate / canUpdateStatus / canClose / canAssignVet / canUnassignVet / canViewClinic |
| `AttachmentPolicy` | canUpload (MIME + size + ägarskap + CLOSED-spärr) / canDownload / canDelete |
| `CommentPolicy` | canCreate / canView / canUpdate / canDelete / isVisibleTo |
| `PetPolicy` | canUpdate / canDelete |
| `ActivityLogPolicy` | canView — endast ADMIN eller inblandade parter |
| `AdminPolicy` | Gate för ADMIN-only operationer |

| Roll    | Behörighet                                                  |
|---------|-------------------------------------------------------------|
| `OWNER` | Ser och hanterar enbart sina egna ärenden och husdjur       |
| `VET`   | Ser och hanterar ärenden knutna till sin klinik             |
| `ADMIN` | Full tillgång till allt                                     |

Policybrott kastar `ForbiddenException` (HTTP 403).

---

## Säkerhet

### Autentisering

- Username/password (email + BCrypt-hash) verifieras via `DaoAuthenticationProvider` + `CustomUserDetailsService`.
- Vid lyckad login utfärdar `JwtService.generateToken()` en signerad JWT (HS256, hemlig nyckel via `JWT_SECRET`).
- Frontend sparar tokenen i `localStorage`/`sessionStorage` och bifogar den som `Authorization: Bearer <token>` på efterföljande requests.

### JWT-claims

Tokenen bär följande claims:

| Claim      | Beskrivning                                                     |
|------------|-----------------------------------------------------------------|
| `sub`      | Användarens email (subject)                                     |
| `userId`   | UUID — backend slår upp `User` i DB vid varje request           |
| `role`     | `ROLE_OWNER` / `ROLE_VET` / `ROLE_ADMIN` — driver URL-filter    |
| `name`     | Användarens namn (för UI-visning)                               |
| `clinicId` | Klinik-UUID (sätts endast för VET) — används i policy-checks    |
| `iat`      | Issued-at timestamp                                             |
| `exp`      | Expiration (default 24 h)                                       |

`JwtAuthenticationFilter` validerar signaturen, kontrollerar `exp`, och hydratiserar `User`-objektet från DB innan controllers nås.

### Stateless

- Ingen server-session (`SessionCreationPolicy.STATELESS`).
- CSRF-skydd avstängt — irrelevant för Bearer-token-baserad auth.

---

## Databasmigration (Flyway)

Schemat hanteras av Flyway-migrationer i `src/main/resources/db/migration/`:

| Migration | Beskrivning |
|---|---|
| `V1__initial_schema.sql` | Grundläggande tabeller (users, clinics, pets, medical_record, comments, attachments, activity_log) |
| `V2__init_orphaned_table.sql` | Tabellen `orphaned_s3_objects` för durable retry (se [Fillagring](#fillagring-minios3)) |
| `dev/V3__insert_demo_data.sql` | Demo-data, **endast** i dev-profilen (`spring.flyway.locations` inkluderar `dev/` för dev-profilen) |

`flyway_schema_history`-tabellen i databasen håller koll på vilka migrationer som körts. Nya migrationer läggs till med nästkommande versionsnummer (`V3__`, `V4__` ...).

---

## Fillagring (MinIO/S3)

Bilagor lagras i MinIO (S3-kompatibelt). Vid applikationsstart skapas bucket automatiskt om den saknas.

**Konfigureras via:** `MinioConfig.java`
**Relevanta env-variabler:** `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`, `S3_REGION`

### Durable retry vid S3-fel (orphan-cleanup)

Vissa S3-operationer kan misslyckas efter att DB-transaktionen redan har committats — då skulle binären annars ligga föräldralös i MinIO utan automatisk återhämtning. För att undvika detta finns en bakgrundsmekanism som garanterar att alla föräldralösa objekt till slut städas upp.

**Komponenter:**

| Klass | Ansvar |
|---|---|
| `OrphanedS3Object` | JPA-entitet för tabellen `orphaned_s3_objects` (`s3_key`, `s3_bucket`, `retry_count`, `last_attempt_at`, `last_error`) |
| `OrphanedS3Enqueuer` | Lägger en S3-nyckel i kön. Kör i `Propagation.REQUIRES_NEW` så raden persisteras även om huvudtransaktionen rullar tillbaka. Idempotent (find-or-create på `s3_key`) |
| `OrphanedS3Processor` | Försöker radera ett objekt i sin egen `REQUIRES_NEW`-transaktion. Lyckat → ta bort kö-raden. Fel → öka `retry_count`, spara felmeddelande |
| `OrphanedS3CleanupWorker` | `@Scheduled(fixedDelay = 600000)` (10 min). Plockar upp till 20 rader åt gången med `NULLS FIRST`-ordning så att nya orphans prioriteras före retries |

**Två triggar för enqueue:**

1. **Upload-cleanup** — om DB-persistens misslyckas efter S3-uppladdning så försöker `AttachmentService` radera direkt; om även det misslyckas läggs nyckeln i kön
2. **Delete efter commit** — `AttachmentService.deleteAttachment` registrerar en `TransactionSynchronization.afterCommit`-callback. Misslyckas S3-anropet där → läggs i kön

**Permanent fail:** Om `retry_count >= MAX_RETRIES` (10) slutar workern försöka och loggar `ALERT`-rad så att operatör kan städa manuellt. Objektet ligger kvar i `orphaned_s3_objects`-tabellen för spårbarhet.

**Migration:** Tabellen skapas via Flyway-migration `V2__init_orphaned_table.sql`.

---

## Testning

Tester är implementerade med JUnit 5 och Mockito.

```bash
./mvnw test
```

**Enhetstester** – finns under `src/test/java/org/example/vet1177/services/` och `policy/`, testar affärslogik och auktorisering med mockade beroenden.

**Integrationstester** – finns under `src/test/java/org/example/vet1177/integration/`, testar mot riktig databas:
- `ClinicIntegrationTest`
- `ActivityLogIntegrationTest`
- `VetIntegrationTest`

---

## CI/CD

GitHub Actions kör bygge och tester automatiskt vid PR mot `main`.

- Workflow: `.github/workflows/`
- Kör `./mvnw verify` vid varje PR

---

## Felhantering

Global felhantering via `GlobalExceptionHandler` (`@RestControllerAdvice`).

| Exception                  | HTTP-status | Beskrivning                        |
|----------------------------|-------------|------------------------------------|
| `ResourceNotFoundException`| 404         | Resursen hittades inte             |
| `ForbiddenException`       | 403         | Behörighet saknas                  |
| `BusinessRuleException`    | 422         | Affärsregelfel                     |
| `BadCredentialsException`  | 401         | Fel email/lösenord vid login       |
| Valideringsfel             | 400         | Ogiltiga request-fält              |
| Övriga fel                 | 500         | Okänt serverfel                    |