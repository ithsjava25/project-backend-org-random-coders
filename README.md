# Vet1177 – Veterinärjournalsystem (Backend)

Ett webbaserat journalhanteringssystem för veterinärkliniker. Husdjursägare kan skapa ärenden, veterinärer kan hantera journaler och bifoga filer, och administratörer har full tillgång till systemet.

---

## Innehåll

- [Tech-stack](#tech-stack)
- [Kom igång](#kom-igång)
- [Miljövariabler](#miljövariabler)
- [Projektstruktur](#projektstruktur)
- [API-endpoints](#api-endpoints)
- [Roller och behörigheter](#roller-och-behörigheter)
- [Fillagring (MinIO/S3)](#fillagring-minios3)
- [Testning](#testning)
- [CI/CD](#cicd)

---

## Tech-stack

| Kategori        | Teknologi                          |
|-----------------|------------------------------------|
| Språk           | Java 25                            |
| Ramverk         | Spring Boot 4.0.4                  |
| Databas         | PostgreSQL 15                      |
| ORM             | Spring Data JPA / Hibernate        |
| Säkerhet        | Spring Security 6                  |
| Fillagring      | MinIO (S3-kompatibel)              |
| Templating      | Thymeleaf                          |
| Loggning        | SLF4J                              |
| Byggsystem      | Maven                              |
| Containerisering| Docker / Docker Compose            |

---

## Kom igång

### Förutsättningar

- Java 25
- Maven
- Docker & Docker Compose

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

**4. Starta applikationen**
```bash
./mvnw spring-boot:run
```

Applikationen startar på `http://localhost:8080`.

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
```

### Entiteter

| Entitet         | Beskrivning                                              |
|-----------------|----------------------------------------------------------|
| `User`          | Användare med roll: `OWNER`, `VET`, `ADMIN`              |
| `Vet`           | Utökar User med licensnummer och specialisering          |
| `Clinic`        | Veterinärklinik                                          |
| `Pet`           | Husdjur knutet till en ägare och klinik                  |
| `MedicalRecord` | Ärende/journal – kärnan i systemet                       |
| `Comment`       | Kommentar på ett ärende                                  |
| `Attachment`    | Bifogad fil lagrad i MinIO/S3                            |
| `ActivityLog`   | Revisionslogg över händelser på ett ärende               |

### Ärendestatus

`OPEN` → `IN_PROGRESS` → `AWAITING_INFO` → `CLOSED`

---

## API-endpoints

Bas-URL: `/api`

### Journaler – `/api/medical-records`

| Metod  | Sökväg                              | Beskrivning                        |
|--------|-------------------------------------|------------------------------------|
| POST   | `/`                                 | Skapa nytt ärende                  |
| GET    | `/{id}`                             | Hämta ärende via ID                |
| GET    | `/my-records`                       | Inloggad ägares ärenden            |
| GET    | `/owner/{ownerId}`                  | Ärenden för en ägare               |
| GET    | `/pet/{petId}`                      | Ärenden för ett husdjur            |
| GET    | `/clinic/{clinicId}`                | Ärenden på en klinik               |
| GET    | `/clinic/{clinicId}/status/{status}`| Filtrera på klinik och status      |
| PUT    | `/{id}`                             | Uppdatera ärende                   |
| PUT    | `/{id}/assign-vet`                  | Tilldela veterinär                 |
| PUT    | `/{id}/status`                      | Uppdatera status                   |
| PUT    | `/{id}/close`                       | Stäng ärende                       |

### Övriga endpoints

| Prefix               | Beskrivning                    |
|----------------------|--------------------------------|
| `/api/clinics`       | CRUD för kliniker              |
| `/api/vets`          | Hämta och skapa veterinärer    |
| `/api/pets`          | Husdjurshantering (pågående)   |
| `/api/comments`      | Kommentarer på ärenden         |
| `/api/activity-logs` | Revisionslogg                  |

---

## Roller och behörigheter

Auktoriseringslogiken hanteras i `MedicalRecordPolicy`.

| Roll    | Behörighet                                                  |
|---------|-------------------------------------------------------------|
| `OWNER` | Ser och hanterar enbart sina egna ärenden och husdjur       |
| `VET`   | Ser och hanterar ärenden knutna till sin klinik             |
| `ADMIN` | Full tillgång till allt                                     |

Policybrott kastar `ForbiddenException` (HTTP 403).

---

## Fillagring (MinIO/S3)

Bilagor lagras i MinIO (S3-kompatibelt). Vid applikationsstart skapas bucket automatiskt om den saknas.

**Konfigureras via:** `MinioConfig.java`  
**Relevanta env-variabler:** `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`, `S3_REGION`

---

### Att göra

| Uppgift                                               | Prioritet |
|-------------------------------------------------------|-----------|
| Admin-policy (auktorisering för adminrollen)          | Hög       |
| Loggning – genomgång och utökning med SLF4J           | Medium    |
| Tester – enhetstester för egna klasser                | Hög       |
| Tester – integrationstester                           | Hög       |
| Fortsätta skapa issues och fördela ansvar             | Löpande   |

### Klart nyligen

- Borttaget dubblett-exceptions-paket
- MinIO-konfiguration ersätter `System.out` med SLF4J-loggning
- Klinik-controller refaktorering
- Pet service policy

---

## Testning

> Tester är ännu inte implementerade – detta är ett prioriterat nästa steg.

Planen är:
1. **Enhetstester** – varje utvecklare skriver tester för sina egna klasser
2. **Integrationstester** – tester mot riktig databas (ingen mocking av DB)

Testberoenden är redan konfigurerade i `pom.xml`:
- `spring-boot-starter-data-jpa-test`
- `spring-boot-starter-webmvc-test`
- `spring-boot-starter-thymeleaf-test`

---

## CI/CD

> CI/CD-pipeline är under uppsättning.

Planerat:
- GitHub Actions workflow för bygge och test vid PR
- Automatisk körning av `./mvnw verify`

---

## Felhantering

Global felhantering via `GlobalExceptionHandler` (`@RestControllerAdvice`).

| Exception                  | HTTP-status | Beskrivning                        |
|----------------------------|-------------|------------------------------------|
| `ResourceNotFoundException`| 404         | Resursen hittades inte             |
| `ForbiddenException`       | 403         | Behörighet saknas                  |
| `BusinessRuleException`    | 400         | Affärsregelfel                     |
| Valideringsfel             | 400         | Ogiltiga request-fält              |
| Övriga fel                 | 500         | Okänt serverfel                    |
