# Vet1177 API — Guide for frontend-utvecklare

Denna guide visar hur du anropar backend-API:et från en React + Tailwind CSS-applikation.
Alla exempel är skrivna för en djurägare (OWNER) som heter Anna.

---

## 1. Kom igang

### Starta backend

Backend startas i IntelliJ. Se till att Docker Desktop kor (behövs for databasen).
Kör `Vet1177Application.java` — servern startar pa `http://localhost:8080`.

### Inloggning under utveckling

Just nu anvander vi en speciell header istället for riktigt login.
Lagg till headern `X-Dev-User` med en e-postadress i varje request.
Det simulerar att du ar inloggad som den anvandaren.

**OBS:** Detta fungerar bara nar backend kör med dev-profilen (vilket ar default lokalt).

### Testanvandare

| Roll  | Namn            | Email           |
|-------|-----------------|-----------------|
| OWNER | Anna Svensson   | anna@test.se    |
| VET   | Erik Veterinär  | erik@klinik.se  |
| ADMIN | Sara Admin      | sara@admin.se   |

Alla testanvandare har lösenordet `password` (hashat i databasen).

---

## 2. Hur man anropar API:et fran React

### Grundmönster

Alla anrop följer samma mönster:

1. Anropa `fetch()` med rätt URL och headers
2. Kolla att `response.ok` ar `true`
3. Läs JSON-datan med `response.json()`

```javascript
// Grundmönster for att hamta data
const hämtaData = async () => {
  const response = await fetch('http://localhost:8080/api/ENDPOINT', {
    headers: {
      'X-Dev-User': 'anna@test.se'   // Talar om vem du ar
    }
  });

  if (!response.ok) {
    throw new Error('Något gick fel: ' + response.status);
  }

  const data = await response.json();
  return data;
};
```

```javascript
// Grundmönster for att skicka data (POST/PUT)
const skickaData = async (body) => {
  const response = await fetch('http://localhost:8080/api/ENDPOINT', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',  // Talar om att vi skickar JSON
      'X-Dev-User': 'anna@test.se'
    },
    body: JSON.stringify(body)  // Gör om JavaScript-objekt till JSON-text
  });

  if (!response.ok) {
    throw new Error('Något gick fel: ' + response.status);
  }

  const data = await response.json();
  return data;
};
```

---

## 3. Endpoints for djurägare (OWNER)

### 3.1 Hamta alla kliniker

Anvands for att visa en dropdown nar djurägaren skapar ett nytt arende.
Alla kliniker ar publika — ingen header behövs.

**`GET /api/clinics`**

```javascript
const getKliniker = async () => {
  const response = await fetch('http://localhost:8080/api/clinics');
  const data = await response.json();
  return data;
};
```

**Svar (lista med kliniker):**

```json
[
  {
    "id": "a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5",
    "name": "Djurkliniken Centrum",
    "address": "Storgatan 1, Stockholm",
    "phoneNumber": "08-123456"
  }
]
```

---

### 3.2 Hamta mina djur

Visar alla djur som tillhör den inloggade djurägaren.

**`GET /pets/owner/{ownerId}`**

```javascript
const getMinaDjur = async () => {
  const ownerId = 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7'; // Annas ID

  const response = await fetch(`http://localhost:8080/pets/owner/${ownerId}`, {
    headers: {
      'X-Dev-User': 'anna@test.se'
    }
  });
  const data = await response.json();
  return data;
};
```

**Svar (lista med djur):**

```json
[
  {
    "id": "f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0",
    "ownerId": "c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7",
    "name": "Fido",
    "species": "Hund",
    "breed": "Labrador",
    "dateOfBirth": "2020-01-15",
    "weightKg": 25.5,
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-01T10:00:00Z"
  },
  {
    "id": "a7b8c9d0-e1f2-4a5b-4c5d-e6f7a8b9c0d1",
    "ownerId": "c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7",
    "name": "Missan",
    "species": "Katt",
    "breed": "Persisk",
    "dateOfBirth": "2019-06-20",
    "weightKg": 4.2,
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-01T10:00:00Z"
  }
]
```

---

### 3.3 Skapa ett djur

Registrera ett nytt djur för den inloggade ägaren.

**`POST /pets`**

```javascript
const skapaDjur = async () => {
  const response = await fetch('http://localhost:8080/pets', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Dev-User': 'anna@test.se'
    },
    body: JSON.stringify({
      name: 'Bella',
      species: 'Hund',
      breed: 'Golden Retriever',
      dateOfBirth: '2022-03-10',
      weightKg: 30.0
    })
  });
  const data = await response.json();
  return data;
};
```

**Request body:**

| Fält        | Typ      | Obligatoriskt | Beskrivning               |
|-------------|----------|---------------|---------------------------|
| name        | string   | Ja            | Djurets namn              |
| species     | string   | Ja            | Art (Hund, Katt, etc.)    |
| breed       | string   | Nej           | Ras                       |
| dateOfBirth | string   | Ja            | Födelsedatum (YYYY-MM-DD) |
| weightKg    | number   | Ja            | Vikt i kg (positivt tal)  |

**Svar:** Samma format som i listan ovan (ett PetResponse-objekt).

---

### 3.4 Hamta mina ärenden

Visar alla veterinärarenden som tillhör den inloggade djurägaren.

**`GET /api/medical-records/my-records`**

```javascript
const getMinaÄrenden = async () => {
  const response = await fetch('http://localhost:8080/api/medical-records/my-records', {
    headers: {
      'X-Dev-User': 'anna@test.se'
    }
  });
  const data = await response.json();
  return data;
};
```

**Svar (lista med ärenden i kortformat):**

```json
[
  {
    "id": "b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2",
    "title": "Fido haltar",
    "status": "OPEN",
    "petName": "Fido",
    "ownerName": "Anna Svensson",
    "assignedVetName": null,
    "createdAt": "2025-01-01T10:00:00Z"
  },
  {
    "id": "c9d0e1f2-a3b4-4c5d-6e7f-a8b9c0d1e2f3",
    "title": "Missan äter inte",
    "status": "IN_PROGRESS",
    "petName": "Missan",
    "ownerName": "Anna Svensson",
    "assignedVetName": "Erik Veterinär",
    "createdAt": "2025-01-01T10:00:00Z"
  }
]
```

Möjliga statusar: `OPEN`, `IN_PROGRESS`, `AWAITING_INFO`, `CLOSED`

---

### 3.5 Skapa ett ärende

Skapar ett nytt veterinärarende. Du behöver ett `petId` (fran 3.2) och ett `clinicId` (fran 3.1).

**`POST /api/medical-records`**

```javascript
const skapaÄrende = async () => {
  const response = await fetch('http://localhost:8080/api/medical-records', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Dev-User': 'anna@test.se'
    },
    body: JSON.stringify({
      title: 'Fido hostar',
      description: 'Hunden har hostat i tre dagar och verkar trött.',
      petId: 'f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0',
      clinicId: 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5'
    })
  });
  const data = await response.json();
  return data;
};
```

**Request body:**

| Fält        | Typ    | Obligatoriskt | Beskrivning                     |
|-------------|--------|---------------|---------------------------------|
| title       | string | Ja            | Kort titel (max 500 tecken)     |
| description | string | Nej           | Beskrivning (max 5000 tecken)   |
| petId       | string | Ja            | UUID for djuret                 |
| clinicId    | string | Ja            | UUID for kliniken               |

**Svar (fullstandigt arende):**

```json
{
  "id": "nytt-uuid-här",
  "title": "Fido hostar",
  "description": "Hunden har hostat i tre dagar och verkar trött.",
  "status": "OPEN",
  "petId": "f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0",
  "petName": "Fido",
  "petSpecies": "Hund",
  "ownerId": "c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7",
  "ownerName": "Anna Svensson",
  "clinicId": "a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5",
  "clinicName": "Djurkliniken Centrum",
  "assignedVetId": null,
  "assignedVetName": null,
  "createdById": "c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7",
  "createdByName": "Anna Svensson",
  "createdAt": "2025-04-14T10:00:00Z",
  "updatedAt": null,
  "closedAt": null
}
```

---

### 3.6 Hamta ett specifikt arende

Visar alla detaljer for ett arende. Anvands nar djurägaren klickar pa ett arende i listan.

**`GET /api/medical-records/{id}`**

```javascript
const getÄrende = async (ärendeId) => {
  const response = await fetch(`http://localhost:8080/api/medical-records/${ärendeId}`, {
    headers: {
      'X-Dev-User': 'anna@test.se'
    }
  });
  const data = await response.json();
  return data;
};

// Anvandning:
const ärende = await getÄrende('b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2');
```

**Svar:** Samma format som i 3.5 (MedicalRecordResponse).

---

### 3.7 Skriva en kommentar

Lagg till en kommentar pa ett arende. Tänk det som en chatt mellan djurägare och veterinär.

**`POST /api/comments`**

```javascript
const skapaKommentar = async (recordId, text) => {
  const response = await fetch('http://localhost:8080/api/comments', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Dev-User': 'anna@test.se'
    },
    body: JSON.stringify({
      recordId: recordId,
      body: text
    })
  });
  const data = await response.json();
  return data;
};

// Anvandning:
await skapaKommentar('b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2', 'Fido verkar bättre idag!');
```

**Request body:**

| Fält     | Typ    | Obligatoriskt | Beskrivning                      |
|----------|--------|---------------|----------------------------------|
| recordId | string | Ja            | UUID for ärendet                 |
| body     | string | Ja            | Kommentarstexten (max 5000 tecken) |

**Svar:**

```json
{
  "id": "nytt-uuid-här",
  "recordId": "b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2",
  "authorId": "c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7",
  "authorName": "Anna Svensson",
  "body": "Fido verkar bättre idag!",
  "createdAt": "2025-04-14T10:00:00Z",
  "updatedAt": null
}
```

---

### 3.8 Hamta kommentarer for ett arende

Visar alla kommentarer pa ett arende, sorterade fran äldst till nyast.

**`GET /api/comments/record/{recordId}`**

```javascript
const getKommentarer = async (recordId) => {
  const response = await fetch(`http://localhost:8080/api/comments/record/${recordId}`, {
    headers: {
      'X-Dev-User': 'anna@test.se'
    }
  });
  const data = await response.json();
  return data;
};

// Anvandning:
const kommentarer = await getKommentarer('b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2');
```

**Svar (lista med kommentarer):**

```json
[
  {
    "id": "kommentar-uuid",
    "recordId": "b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2",
    "authorId": "c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7",
    "authorName": "Anna Svensson",
    "body": "Fido verkar bättre idag!",
    "createdAt": "2025-04-14T10:00:00Z",
    "updatedAt": null
  }
]
```

---

### 3.9 Bifoga en fil

Ladda upp en bild eller PDF till ett arende. Bilagor skickas som `multipart/form-data` — inte JSON.
Det ar samma format som ett vanligt HTML-formulär med `<input type="file">`.

**`POST /api/attachments/record/{recordId}`**

```javascript
const laddaUppFil = async (recordId, file, beskrivning) => {
  // FormData anvands istället for JSON nar man skickar filer
  const formData = new FormData();
  formData.append('file', file);               // file = File-objekt fran <input type="file">
  formData.append('description', beskrivning); // Valfri beskrivning

  const response = await fetch(`http://localhost:8080/api/attachments/record/${recordId}`, {
    method: 'POST',
    headers: {
      'X-Dev-User': 'anna@test.se'
      // OBS: Satt INTE 'Content-Type' här — browsern lägger till det automatiskt
      // med rätt boundary for multipart/form-data
    },
    body: formData  // Skicka FormData direkt, INTE JSON.stringify()
  });
  const data = await response.json();
  return data;
};

// Anvandning i en React-komponent:
// <input type="file" onChange={(e) => laddaUppFil(recordId, e.target.files[0], 'Rontgenbild')} />
```

**Tillåtna filtyper:** JPG, PNG, PDF
**Maxstorlek:** 10 MB

**Svar (201 Created):**

```json
{
  "id": "bilaga-uuid",
  "recordId": "b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2",
  "fileName": "rontgen_fido.jpg",
  "description": "Rontgenbild",
  "fileType": "image/jpeg",
  "fileSizeBytes": 245000,
  "uploadedAt": "2025-04-14T10:00:00Z",
  "uploadedBy": "Anna Svensson",
  "downloadUrl": "https://minio.../presigned-url"
}
```

---

### 3.10 Hamta bilagor for ett arende

Visar alla uppladdade filer pa ett arende. Varje bilaga har en `downloadUrl` som du kan anvanda som `src` i en `<img>` eller som `href` i en `<a>`.

**`GET /api/attachments/record/{recordId}`**

```javascript
const getBilagor = async (recordId) => {
  const response = await fetch(`http://localhost:8080/api/attachments/record/${recordId}`, {
    headers: {
      'X-Dev-User': 'anna@test.se'
    }
  });
  const data = await response.json();
  return data;
};
```

**Svar (lista med bilagor):**

```json
[
  {
    "id": "bilaga-uuid",
    "recordId": "b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2",
    "fileName": "rontgen_fido.jpg",
    "description": "Rontgenbild",
    "fileType": "image/jpeg",
    "fileSizeBytes": 245000,
    "uploadedAt": "2025-04-14T10:00:00Z",
    "uploadedBy": "Anna Svensson",
    "downloadUrl": "https://minio.../presigned-url"
  }
]
```

**Tips:** `downloadUrl` ar en tidsbegränsad länk (giltig i 15 minuter). Om bilden inte laddas, hamta bilagorna igen for att fa en ny URL.

---

## 4. Statuskoder att känna till

Nar du anropar API:et far du alltid tillbaka en HTTP-statuskod. Kolla den i `response.status`.

| Kod | Namn             | Vad det betyder                                              |
|-----|------------------|--------------------------------------------------------------|
| 200 | OK               | Allt fungerade. Datan finns i response body.                 |
| 201 | Created          | Resursen skapades. Datan finns i response body.              |
| 204 | No Content       | Lyckades, men inget data i svaret (t.ex. vid delete).        |
| 400 | Bad Request      | Något ar fel i din request. Kolla att alla fält ar ifyllda.  |
| 403 | Forbidden        | Du har inte rättighet. T.ex. en OWNER som försöker nå en VET-endpoint. |
| 404 | Not Found        | Resursen finns inte. Kolla att UUID:t stämmer.               |
| 500 | Server Error     | Något gick fel i backend. Kolla IntelliJ-loggen.             |

**Felmeddelanden** kommer som JSON:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "title": ["Titel far inte vara tom"]
  }
}
```

---

## 5. Nar JWT ar implementerat

Just nu anvander vi `X-Dev-User`-headern for att simulera inloggning. Nar JWT ar klart byter du ut den mot en riktig token.

### Före (nu — utvecklingsläge)

```javascript
headers: {
  'X-Dev-User': 'anna@test.se'
}
```

### Efter (med JWT)

```javascript
// 1. Logga in och fa en token
const login = async (email, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  return data.token;  // Spara denna i state eller localStorage
};

// 2. Anvand token i alla andra anrop
headers: {
  'Authorization': `Bearer ${token}`
}
```

**Allt annat ar likadant** — samma URLer, samma request bodies, samma svar. Bara headern andras.

---

## 6. Kända test-UUID:n

Dessa UUID:n finns i testdatan som laddas vid uppstart. Anvand dem for att testa utan att behöva skapa data först.

### Klinik

| Namn                  | UUID                                   |
|-----------------------|----------------------------------------|
| Djurkliniken Centrum  | `a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5` |

### Anvandare

| Namn            | Roll  | Email          | UUID                                   |
|-----------------|-------|----------------|----------------------------------------|
| Anna Svensson   | OWNER | anna@test.se   | `c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7` |
| Erik Veterinär  | VET   | erik@klinik.se | `d4e5f6a7-b8c9-4d5e-1f2a-b3c4d5e6f7a8` |
| Sara Admin      | ADMIN | sara@admin.se  | `e5f6a7b8-c9d0-4e5f-2a3b-c4d5e6f7a8b9` |

### Djur

| Namn   | Art  | Ras       | UUID                                   |
|--------|------|-----------|----------------------------------------|
| Fido   | Hund | Labrador  | `f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0` |
| Missan | Katt | Persisk   | `a7b8c9d0-e1f2-4a5b-4c5d-e6f7a8b9c0d1` |

### Arenden

| Titel           | Status      | UUID                                   |
|-----------------|-------------|----------------------------------------|
| Fido haltar     | OPEN        | `b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2` |
| Missan äter inte | IN_PROGRESS | `c9d0e1f2-a3b4-4c5d-6e7f-a8b9c0d1e2f3` |

---

## 7. Alla endpoints — komplett lista

Nedan ar samtliga endpoints i API:et, grupperade per controller.
Kolumnen "Auth" visar vem som kan anropa endpointen.

**Förkortningar:**
- **Alla** = ingen inloggning krävs (permitAll)
- **Inloggad** = alla inloggade anvandare
- **OWNER** = djurägare
- **VET** = veterinär
- **ADMIN** = administratör
- **Policy** = rollkontroll sker i service/policy-lagret, inte i controllern

### Autentisering (`/api/auth`)

*Dessa endpoints finns ännu inte — de skapas av Person B (se issue #1–3).*

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| POST | `/api/auth/register` | Alla | Registrera ny anvandare | `{ name, email, password, role }` |
| POST | `/api/auth/login` | Alla | Logga in, fa JWT-token | `{ email, password }` |

### Kliniker (`/api/clinics`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| GET | `/api/clinics` | Alla | Hamta alla kliniker | — |
| GET | `/api/clinics/{id}` | Alla | Hamta en klinik | — |
| POST | `/api/clinics` | Inloggad | Skapa klinik | `{ name, address, phoneNumber }` |
| PUT | `/api/clinics/{id}` | Inloggad | Uppdatera klinik | `{ name, address, phoneNumber }` |
| DELETE | `/api/clinics/{id}` | Inloggad | Ta bort klinik | — |

**Svar (ClinicResponse):**
```json
{ "id": "uuid", "name": "...", "address": "...", "phoneNumber": "..." }
```

### Djur (`/pets`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| POST | `/pets` | OWNER/ADMIN (Policy) | Skapa djur. Admin kan ange `?ownerId=uuid` | `{ name, species, breed, dateOfBirth, weightKg }` |
| GET | `/pets/{petId}` | OWNER/VET/ADMIN (Policy) | Hamta ett djur | — |
| GET | `/pets/owner/{ownerId}` | OWNER/ADMIN (Policy) | Hamta alla djur for en ägare | — |
| PUT | `/pets/{petId}` | OWNER/ADMIN (Policy) | Uppdatera djurinfo | `{ name, species, breed, dateOfBirth, weightKg }` |
| DELETE | `/pets/{petId}` | OWNER/ADMIN (Policy) | Ta bort djur | — |

**Svar (PetResponse):**
```json
{
  "id": "uuid", "ownerId": "uuid", "name": "Fido", "species": "Hund",
  "breed": "Labrador", "dateOfBirth": "2020-01-15", "weightKg": 25.5,
  "createdAt": "2025-01-01T10:00:00Z", "updatedAt": "2025-01-01T10:00:00Z"
}
```

### Arenden (`/api/medical-records`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| POST | `/api/medical-records` | Inloggad (Policy) | Skapa arende | `{ title, description, petId, clinicId }` |
| GET | `/api/medical-records/{id}` | Inloggad (Policy) | Hamta ett arende (fullstandig) | — |
| GET | `/api/medical-records/my-records` | OWNER | Hamta mina ärenden (kortformat) | — |
| GET | `/api/medical-records/owner/{ownerId}` | OWNER (egna)/VET/ADMIN | Hamta ärenden per ägare | — |
| GET | `/api/medical-records/pet/{petId}` | Inloggad (Policy) | Hamta ärenden per djur | — |
| GET | `/api/medical-records/clinic/{clinicId}` | VET/ADMIN (Policy) | Hamta ärenden per klinik | — |
| GET | `/api/medical-records/clinic/{clinicId}/status/{status}` | VET/ADMIN (Policy) | Hamta ärenden per klinik + status | — |
| PUT | `/api/medical-records/{id}` | VET/ADMIN (Policy) | Uppdatera titel/beskrivning | `{ title, description }` |
| PUT | `/api/medical-records/{id}/assign-vet` | VET/ADMIN (Policy) | Tilldela veterinär | `{ vetId }` |
| PUT | `/api/medical-records/{id}/status` | VET/ADMIN (Policy) | Ändra status | `{ status }` |
| PUT | `/api/medical-records/{id}/close` | VET/ADMIN (Policy) | Stäng ärende | — |

**Möjliga statusar:** `OPEN`, `IN_PROGRESS`, `AWAITING_INFO`, `CLOSED`

**Svar — fullständigt (MedicalRecordResponse):**
```json
{
  "id": "uuid", "title": "...", "description": "...", "status": "OPEN",
  "petId": "uuid", "petName": "Fido", "petSpecies": "Hund",
  "ownerId": "uuid", "ownerName": "Anna Svensson",
  "clinicId": "uuid", "clinicName": "Djurkliniken Centrum",
  "assignedVetId": null, "assignedVetName": null,
  "createdById": "uuid", "createdByName": "Anna Svensson",
  "createdAt": "2025-01-01T10:00:00Z", "updatedAt": null, "closedAt": null
}
```

**Svar — kortformat (MedicalRecordSummaryResponse):**
```json
{
  "id": "uuid", "title": "...", "status": "OPEN",
  "petName": "Fido", "ownerName": "Anna Svensson",
  "assignedVetName": null, "createdAt": "2025-01-01T10:00:00Z"
}
```

### Kommentarer (`/api/comments`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| POST | `/api/comments` | Inloggad (Policy) | Skapa kommentar | `{ recordId, body }` |
| GET | `/api/comments/record/{recordId}` | Inloggad (Policy) | Hamta kommentarer for ett arende | — |
| GET | `/api/comments/record/{recordId}/count` | Inloggad (Policy) | Antal kommentarer pa ett arende | — |
| PUT | `/api/comments/{id}` | Inloggad (Policy) | Uppdatera kommentar | `{ body }` |
| DELETE | `/api/comments/{id}` | Inloggad (Policy) | Ta bort kommentar | — |

**Svar (CommentResponse):**
```json
{
  "id": "uuid", "recordId": "uuid", "authorId": "uuid",
  "authorName": "Anna Svensson", "body": "Fido verkar bättre!",
  "createdAt": "2025-04-14T10:00:00Z", "updatedAt": null
}
```

### Bilagor (`/api/attachments`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| POST | `/api/attachments/record/{recordId}` | Inloggad (Policy) | Ladda upp fil (multipart/form-data) | `file` + valfri `description` |
| GET | `/api/attachments/record/{recordId}` | Inloggad (Policy) | Hamta bilagor for ett arende | — |
| GET | `/api/attachments/{id}/download` | Inloggad (Policy) | Hamta en bilaga (presigned URL) | — |
| DELETE | `/api/attachments/{id}` | VET/ADMIN (Policy) | Ta bort bilaga | — |

**Tillåtna filtyper:** JPG, PNG, PDF. **Max:** 10 MB.

**Svar (AttachmentResponse):**
```json
{
  "id": "uuid", "recordId": "uuid", "fileName": "bild.jpg",
  "description": "Rontgenbild", "fileType": "image/jpeg",
  "fileSizeBytes": 245000, "uploadedAt": "2025-04-14T10:00:00Z",
  "uploadedBy": "Anna Svensson", "downloadUrl": "https://presigned-url..."
}
```

### Aktivitetslogg (`/api/activity-logs`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| GET | `/api/activity-logs/record/{recordId}` | Inloggad (Policy) | Hamta händelselogg for ett arende | — |

**Svar (lista med ActivityLogResponse):**
```json
[
  {
    "id": "uuid", "action": "CASE_CREATED", "description": "Ärende skapat",
    "performedById": "uuid", "performedByName": "Anna Svensson",
    "recordId": "uuid", "createdAt": "2025-01-01T10:00:00Z"
  }
]
```

**Möjliga actions:** `CASE_CREATED`, `UPDATED`, `STATUS_CHANGED`, `ASSIGNED`, `COMMENT_ADDED`

### Anvandare (`/api/users`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| GET | `/api/users` | Inloggad | Hamta alla anvandare | — |
| GET | `/api/users/{id}` | Inloggad | Hamta en anvandare | — |
| POST | `/api/users` | Inloggad | Skapa anvandare | `{ name, email, password, role, clinicId? }` |
| PUT | `/api/users/{id}` | Inloggad | Uppdatera anvandare | `{ name?, email?, clinicId? }` |
| DELETE | `/api/users/{id}` | Inloggad | Ta bort anvandare | — |

**Svar (UserResponse):**
```json
{
  "id": "uuid", "name": "Anna Svensson", "email": "anna@test.se",
  "role": "OWNER", "clinicId": null,
  "createdAt": "2025-01-01T10:00:00Z", "updatedAt": "2025-01-01T10:00:00Z"
}
```

### Veterinärer (`/api/vets`)

| Metod | URL | Auth | Beskrivning | Request body |
|-------|-----|------|-------------|--------------|
| GET | `/api/vets` | Inloggad | Hamta alla veterinärer | — |
| GET | `/api/vets/{id}` | Inloggad | Hamta en veterinär | — |
| POST | `/api/vets` | ADMIN | Skapa veterinärprofil | `{ userId, licenseId, specialization?, bookingInfo? }` |

**Svar (VetResponse):**
```json
{
  "userId": "uuid", "name": "Erik Veterinär", "email": "erik@klinik.se",
  "licenseId": "VET-001", "specialization": "Ortopedi",
  "bookingInfo": "Mån-Fre 08-17", "clinicName": "Djurkliniken Centrum",
  "isActive": true
}
