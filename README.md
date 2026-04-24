# Vet1177 — Digital veterinärvård

Ett webbaserat journalhanteringssystem för veterinärkliniker, inspirerat av 1177.se. Djurägare registrerar ärenden för sina djur och kommunicerar med kliniken. Veterinärer tar ärenden, dokumenterar journaler och bifoga filer. Administratörer hanterar kliniker och användare.

Projektet är fullstack: Spring Boot-backend med PostgreSQL + MinIO, och React-frontend.

---

## Innehåll

- [Arkitektur](#arkitektur)
- [Tech-stack](#tech-stack)
- [Kom igång](#kom-igång)
- [Miljövariabler](#miljövariabler)
- [Projektstruktur](#projektstruktur)
- [Domänmodell](#domänmodell)
- [API](#api)
- [Roller och behörigheter](#roller-och-behörigheter)
- [Säkerhet](#säkerhet)
- [Fillagring (MinIO/S3)](#fillagring-minios3)
- [Felhantering](#felhantering)
- [Testning](#testning)
- [CI/CD](#cicd)
- [Pågående och planerade arbeten](#pågående-och-planerade-arbeten)

---

## Arkitektur

```
┌─────────────────────┐        JWT via Authorization header        ┌─────────────────────┐
│                     │  ─────────────────────────────────────▶   │                     │
│   React SPA         │                                            │   Spring Boot       │
│   (Vite + Tailwind) │  ◀─────────────────────────────────────   │   REST API          │
│                     │           JSON responses                   │                     │
└─────────────────────┘                                            └──────────┬──────────┘
                                                                              │
                                                         ┌────────────────────┼────────────────────┐
                                                         ▼                    ▼                    ▼
                                                  ┌─────────────┐     ┌──────────────┐     ┌──────────────┐
                                                  │ PostgreSQL  │     │ MinIO / S3   │     │ JWT-signed   │
                                                  │ (schema.sql)│     │ (attachments)│     │ tokens       │
                                                  └─────────────┘     └──────────────┘     └──────────────┘
```

- Backend är en stateless REST-tjänst. Inga server-sessioner — identiteten bärs i JWT.
- Frontend är en separat SPA som bara pratar med backend via REST.
- Fillagring sker utanför relationsdatabasen: DB håller metadata, objekt-binärer ligger i MinIO (S3-kompatibel).
- Auktorisering är tvåstegs: grovmaskigt rollfilter på URL-nivå i Spring Security, finkornig ägarskap/kliniktillhörighet i dedikerade policy-klasser.

---

## Tech-stack

### Backend

| Kategori | Teknologi |
|---|---|
| Språk | Java 24 |
| Ramverk | Spring Boot 4.0.4 (MVC, Data JPA, Validation) |
| Säkerhet | Spring Security 7 + `spring-boot-starter-oauth2-resource-server` (JWT-validering) |
| Databas | PostgreSQL 15 |
| ORM | Hibernate (via Spring Data JPA) |
| Migration | `schema.sql` via `spring.sql.init` (Flyway planerad — se [Pågående arbete](#pågående-och-planerade-arbeten)) |
| Fillagring | MinIO (S3-kompatibel) via AWS SDK for Java v2 |
| Serialisering | Jackson Databind |
| Loggning | SLF4J |
| Test | JUnit 5, Mockito, AssertJ, Spring Security Test, `@WebMvcTest` |
| Coverage | JaCoCo |
| Byggsystem | Maven (med wrapper `mvnw`) |
| Containerisering | Docker Compose (Postgres + MinIO) |

### Frontend

| Kategori | Teknologi |
|---|---|
| Ramverk | React 19 |
| Build | Vite 8 |
| Styling | Tailwind CSS 3 |
| HTTP | Axios (med request/response-interceptors för JWT och 401/403-hantering) |
| JWT parsing | `jwt-decode` |
| Ikoner | Lucide React |
| Linting | ESLint 9 |

---

## Kom igång

### Förutsättningar

- Java 24
- Maven (wrapper finns i repot)
- Docker & Docker Compose
- Node.js 18+ och npm (för frontend)

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

**5. Starta frontend (i ny terminal)**
```bash
cd frontend
npm install
npm run dev
```
Frontenden startar på `http://localhost:5173`.

> MinIO-konsolen nås på `http://localhost:9001` (använd dina `MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD`).

---

## Miljövariabler

| Variabel | Beskrivning | Exempel |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/vet1177` |
| `DB_USERNAME` | Databasanvändare | `postgres` |
| `DB_PASSWORD` | Databaslösenord | – |
| `DB_NAME` | Databasnamn | `vet1177` |
| `S3_ENDPOINT` | MinIO/S3-URL | `http://localhost:9000` |
| `S3_ACCESS_KEY` | S3 access key | – |
| `S3_SECRET_KEY` | S3 secret key | – |
| `S3_BUCKET` | Bucket-namn för bilagor | `vet1177-attachments` |
| `S3_REGION` | S3-region | `eu-north-1` |
| `MINIO_ROOT_USER` | MinIO admin-användare | – |
| `MINIO_ROOT_PASSWORD` | MinIO admin-lösenord | – |
| `JWT_SECRET` | Hemlig nyckel för JWT-signering (min. 32 tecken) | – |

`jwt.expiration-ms` konfigureras i `application.properties` (default 24 h). Frontenden läser `VITE_API_BASE_URL` — defaultar till `http://localhost:8080/api`.

---

## Projektstruktur

```
.
├── src/main/java/org/example/vet1177/
│   ├── config/              # AWS/MinIO-konfiguration
│   ├── controller/          # REST-controllers (Medical, Attachment, Comment, Pet, Clinic, Vet, User, ActivityLog)
│   ├── dto/
│   │   ├── request/         # Inkommande DTO:er med Bean Validation
│   │   └── response/        # Utgående DTO:er
│   ├── entities/            # JPA-entiteter + enums (Role, RecordStatus, ActivityType, CommentType)
│   ├── exception/           # Custom exceptions + GlobalExceptionHandler
│   ├── policy/              # Auktoriseringsregler per domänobjekt (6 policies)
│   ├── repository/          # Spring Data JPA-repositories
│   ├── security/            # JwtService, JwtAuthenticationFilter, CustomUserDetailsService,
│   │                        # SecurityConfig, DevSecurityConfig, AuthService, AuthController
│   └── services/            # Affärslogik
├── src/main/resources/
│   ├── schema.sql           # Tabelldefinitioner (idempotent med IF NOT EXISTS)
│   ├── data.sql             # Seed-data för dev
│   ├── application.properties
│   └── application-dev.properties
├── src/test/java/...        # 500+ tester: policy, service, controller, entity, integration
├── frontend/
│   ├── src/
│   │   ├── pages/           # CaseDetail, OwnerDashboard, VetDashboard, AdminDashboard, Login, Register, PetDetail, CreateCase
│   │   ├── components/      # Layout, PetForm, PetList, admin/*
│   │   ├── services/api.jsx # Axios-instans med interceptors
│   │   └── utils/statusHelper.js  # Central svensk-mappning för status + ActivityType
│   └── package.json
├── docker-compose.yml       # Postgres 15 + MinIO
├── .github/workflows/ci.yml # GitHub Actions
├── API.md                   # Detaljerad API-dokumentation
└── pom.xml
```

---

## Domänmodell

### Entiteter

| Entitet | Beskrivning |
|---|---|
| `User` | Användare med roll (`OWNER`, `VET`, `ADMIN`), ev. kopplad till en klinik |
| `Vet` | Veterinärprofil — kompletterar `User` med licens-ID och specialisering |
| `Clinic` | Klinik med adress, telefon, e-post |
| `Pet` | Djur kopplat till en ägare |
| `MedicalRecord` | Ärende/journal — kärnenhet. Har status, ägare, klinik, ev. tilldelad VET |
| `Comment` | Meddelande på ett ärende, typad som `OWNER_MESSAGE` eller `VET_CLINICAL_NOTE` |
| `Attachment` | Metadata för en bifogad fil — binären ligger i MinIO |
| `ActivityLog` | Revisionsspår av händelser på ärenden (separat från `medical_record`-tabellen) |

### Ärendestatus

Status är en enum (`RecordStatus`):

- `OPEN` — nytt ärende, inte tilldelat
- `IN_PROGRESS` — handläggning pågår (sätts automatiskt vid `assignVet`)
- `AWAITING_INFO` — väntar på komplettering från ägaren
- `CLOSED` — avslutat (slutstatus, kräver slutnotering)

VET kan hoppa mellan `OPEN`, `IN_PROGRESS` och `AWAITING_INFO`. Stängning sker via en dedikerad endpoint som kräver en slutnotering.

### Aktivitetstyper

`ActivityType`-enumet används i revisionsspåret: `CASE_CREATED`, `STATUS_CHANGED`, `COMMENT_ADDED`, `ASSIGNED`, `UNASSIGNED`, `UPDATED`.

---

## API

Bas-URL: `/api`. Alla endpoints kräver JWT i `Authorization: Bearer <token>` utom `/api/auth/**` och `GET /api/clinics/**`.

Fullständig dokumentation finns i [`API.md`](./API.md).

### Autentisering — `/api/auth`

| Metod | Sökväg | Beskrivning |
|---|---|---|
| POST | `/login` | Logga in. Returnerar JWT + userinfo |
| POST | `/register` | Registrera ny OWNER. Returnerar JWT + userinfo |

### Journaler — `/api/medical-records`

| Metod | Sökväg | Beskrivning |
|---|---|---|
| POST | `/` | Skapa nytt ärende |
| GET | `/{id}` | Hämta ärende |
| GET | `/my-records` | Inloggad ägares ärenden |
| GET | `/my-assigned` | Inloggad veterinärs tilldelade ärenden |
| GET | `/owner/{ownerId}` | Ärenden för en ägare |
| GET | `/pet/{petId}` | Ärenden för ett djur |
| GET | `/clinic/{clinicId}` | Ärenden på en klinik |
| GET | `/clinic/{clinicId}/status/{status}` | Filtrera klinik + status |
| PUT | `/{id}` | Uppdatera titel/beskrivning (OWNER på eget, VET/ADMIN) |
| PUT | `/{id}/assign-vet` | Tilldela veterinär |
| PUT | `/{id}/unassign-vet` | Tilldelad VET släpper ärendet |
| PUT | `/{id}/status` | Ändra status (VET/ADMIN) |
| PUT | `/{id}/close` | Stäng ärende (VET/ADMIN) |

### Övriga prefix

| Prefix | Beskrivning |
|---|---|
| `/api/pets` | CRUD för djur |
| `/api/attachments` | Ladda upp, lista, hämta (med presigned URL), radera bilagor |
| `/api/comments` | Kommentera ärenden |
| `/api/activity-logs` | Revisionsspår per ärende / globalt (ADMIN) |
| `/api/clinics` | CRUD för kliniker (ADMIN) |
| `/api/users` | User management (ADMIN) |
| `/api/vets` | Veterinärprofiler |

---

## Roller och behörigheter

Auktoriseringen sker i två lager:

**Lager 1 — URL-nivå** (`SecurityConfig`): grovmaskigt rollfilter via `requestMatchers(...).hasAnyRole(...)`. T.ex. `DELETE /api/attachments/**` kräver `VET` eller `ADMIN`.

**Lager 2 — Policy-klasser** (ett per domänobjekt): finkornig ägarskap / kliniktillhörighet / statuschecks. Kallas från service/controller innan mutationer.

| Policy-klass | Ansvar |
|---|---|
| `MedicalRecordPolicy` | canCreate / canView / canUpdate / canUpdateStatus / canClose / canAssignVet / canUnassignVet / canViewClinic |
| `AttachmentPolicy` | canUpload (MIME + size + ägarskap + CLOSED-spärr) / canDownload / canDelete |
| `CommentPolicy` | canCreate / canView / canUpdate / canDelete / isVisibleTo |
| `PetPolicy` | canUpdate / canDelete |
| `ActivityLogPolicy` | canView — endast ADMIN eller inblandade parter |
| `AdminPolicy` | Gate för ADMIN-only operationer |

### Rollmatris (sammanfattning)

| Handling | OWNER | VET | ADMIN |
|---|---|---|---|
| Skapa ärende för eget djur | ✅ | ✅ (egen klinik) | ✅ |
| Skapa ärende för annans djur | ❌ 403 | ✅ (egen klinik) | ✅ |
| Läsa eget ärende | ✅ | ✅ (samma klinik) | ✅ |
| Uppdatera titel/beskrivning | ✅ (eget) | ✅ (samma klinik) | ✅ |
| Ändra status / tilldela VET / stänga | ❌ 403 | ✅ | ✅ |
| Släppa tilldelat ärende | ❌ | ✅ (bara egen self-assign) | ✅ |
| Kommentera eget öppet ärende | ✅ (`OWNER_MESSAGE`) | ✅ (`VET_CLINICAL_NOTE`) | ✅ |
| Se VET:ens journalanteckningar på eget ärende | ✅ | ✅ | ✅ |
| Ladda upp bilaga på eget öppet ärende | ✅ | ✅ (även stängda för arkivering) | ✅ |
| Radera bilaga | ❌ 403 | ✅ (egen uppladdning, samma klinik) | ✅ |

Policybrott kastar `ForbiddenException` → HTTP 403.

---

## Säkerhet

### Autentisering

- Username/password (email + BCrypt-hash) verifieras via `DaoAuthenticationProvider` + `CustomUserDetailsService`.
- Vid lyckad login utfärdar `JwtService.generateToken()` en JWT med claims: `sub` (email), `userId`, `role`, `name`, ev. `clinicId`, `iat`, `exp`.
- Signering: HS256 med symmetrisk nyckel från `JWT_SECRET` (min. 32 tecken).
- Standard expiration: 24 h.

### Hur tokenen används

- Frontend sparar tokenen i `localStorage` (med "Kom ihåg mig") eller `sessionStorage`.
- Axios-interceptor lägger på `Authorization: Bearer <token>` på varje request.
- Backend: `JwtAuthenticationFilter` extraherar token, validerar signatur + `exp`, hämtar `User` från DB via `userId`, sätter `SecurityContextHolder`.

### Stateless

- Ingen server-session (`SessionCreationPolicy.STATELESS`).
- CSRF avstängt — irrelevant för Bearer-token-auth. Detta beslut är medvetet dokumenterat i `SecurityConfig`.

### Inte i scope (idag)

- Rate limiting / brute-force-skydd på `/api/auth/login`
- Refresh tokens / token-revokering
- Passkeys / OAuth2-social-login
- Separat audit-logg för säkerhetshändelser (failed logins, 403-försök) — domän-audit finns i `activity_log`

---

## Fillagring (MinIO/S3)

Bilagor lagras i MinIO (S3-kompatibel). Vid applikationsstart skapas bucket:en om den saknas.

**Nedladdning:** `GET /api/attachments/{id}/download` returnerar en **presigned URL** med kort livslängd. Policy (`canDownload`) körs innan URL:en genereras, så orättmätig access blockeras redan på backend-sidan.

**Uppladdning:** multipart/form-data, MIME valideras mot en allowlist (JPG/PNG/PDF), max 10 MB.

**Radering:** `AttachmentService.deleteAttachment()` tar bort DB-raden i en transaktion och försöker därefter radera S3-objektet. **Om S3-deletion misslyckas efter commit är objektet idag föräldralöst** — se [Pågående arbete](#pågående-och-planerade-arbeten).

---

## Felhantering

Global exception handling via `GlobalExceptionHandler` (`@RestControllerAdvice`).

| Exception | HTTP | Beskrivning |
|---|---|---|
| `ResourceNotFoundException` | 404 | Resursen hittades inte |
| `ForbiddenException` | 403 | Behörighet saknas (från policy) |
| `BusinessRuleException` | 422 | Affärsregelfel (t.ex. stängt ärende, dubblett) |
| `BadCredentialsException` | 401 | Fel lösenord vid login |
| Valideringsfel (`MethodArgumentNotValid`, `ConstraintViolation`) | 400 | Ogiltiga request-fält |
| `AccessDeniedException` | 403 | Spring Securitys egen (mappas separat) |
| Okänt undantag | 500 | Fallback |

Alla felsvar är JSON med `status`, `error`, `message`.

---

## Testning

**~500 tester** körs via `./mvnw test`. Fördelning:

- **Policy-tester** — alla sex policy-klasser har uttömmande rollmatriser
- **Service-tester** (Mockito) — alla service-klasser
- **Controller-tester** (`@WebMvcTest` + MockMvc + `SecurityMockMvcRequestPostProcessors`)
- **Entity-tester** — invariants och relations
- **Integrationstester** — `VetIntegrationTest`, `ClinicIntegrationTest`, `ActivityLogIntegrationTest` (H2 in-memory med PostgreSQL-kompatibelt läge)

Täckning rapporteras av JaCoCo (`target/site/jacoco/index.html` efter `./mvnw verify`).

Ej i scope idag: end-to-end-tester (Playwright/Cypress), mutationstestning, Testcontainers mot riktig Postgres.

---

## CI/CD

`.github/workflows/ci.yml` körs på varje push till `main` och varje PR mot `main`.

Pipeline:

1. Checkout
2. Setup JDK (Temurin 25)
3. Cache `~/.m2`
4. `mvn clean verify` — kompilering + alla tester
5. Upload JAR-artefakt (endast vid push till `main`)

Inget automatiserat deploy-steg (deploy görs manuellt idag).

---

## Pågående och planerade arbeten

### 🚧 Flyway-migration

Databasmigration sker idag via `spring.sql.init` som kör `schema.sql` vid start. Schemat är skrivet idempotent (`CREATE TABLE IF NOT EXISTS` + `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`) för att klara återkörning, men det skalar inte för många iterationer och historiken är inte spårbar i databasen.

**Plan:**
- Introducera Flyway med versionerade migrationer (`V1__init.sql`, `V2__add_comment_type.sql`, `V3__add_attachment_description.sql` osv.)
- Baseline mot befintligt schema så att existerande databaser kan adoptera Flyway utan att förlora data
- `flyway_schema_history`-tabell ger tydlig körnings-logg
- Ta bort `spring.sql.init`-mekanismen när Flyway är live

### 🚧 Durable retry för S3-radering

**Problem:** När `AttachmentService.deleteAttachment()` tar bort DB-raden lyckas — men det efterföljande MinIO-anropet misslyckas (nätverksfel, transient fel) — loggas felet och DB-transaktionen är redan commit:ad. Filen ligger då föräldralös i MinIO med ingen automatisk återhämtning.

**Plan:**
- Införa en `pending_deletions`-tabell som skriver ned `s3Key` i samma transaktion som metadata-raderingen
- En bakgrundsjob (Spring `@Scheduled`) plockar rader med retry-räknare och exponential backoff
- Lyckad deletion → radera raden. Misslyckad efter N försök → flagga för manuell översyn
- Metrik/larm på backlog-storlek

### ♻️ Övrigt på backloggen

| Område | Beskrivning |
|---|---|
| Rate limiting | Skydd mot brute-force på `/api/auth/login` (Bucket4j eller Spring Security:s `loginAttempts`) |
| Audit-logg för säkerhetshändelser | Separat från `activity_log` — failed logins, 403-försök, roll-ändringar |
| E2E-tester | Playwright eller Cypress mot staging-miljö |
| Testcontainers | Byta H2 mot riktig PostgreSQL i integrationstester |
| i18n | Flera språk i frontend (idag bara svenska hårdkodat) |
| Statisk analys | Spotless / Checkstyle för backend |
| CD-pipeline | Automatiskt deploy till staging vid merge till main |
