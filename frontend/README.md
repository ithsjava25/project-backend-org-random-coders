# Vet1177 - Frontend (React + Vite)

Denna del av projektet är byggd med **React 18**, **Vite** och **Tailwind CSS**. Vi har nu implementerat ett säkert autentiseringsflöde med JWT (JSON Web Tokens).

##  Kom igång

För att köra frontend lokalt behöver du ha **Node.js** installerat på din dator.
Rekommenderad version: Node.js 22 LTS eller senare.

### 1. Installera beroenden
Första gången du kör projektet (eller efter att du gjort en `git pull`), kör följande kommando i frontend-mappen:

```bash
npm install
```
### 2. Starta utvecklingsservern
För att starta appen med "Hot Module Replacement" (sidan uppdateras direkt när du sparar)
Se till at du är i frontend-mappen när du kör detta:
````Bash
npm run dev
````
Appen körs normalt på: http://localhost:5173

### Autentisering & Säkerhet

Vi använder JWT för att kommunicera säkert med backenden.

Inloggning: Sker via /login. Vid lyckad inloggning sparas en token i webbläsarens localStorage.

API-anrop: Vi använder en Axios-interceptor (se src/services/api.js) som automatiskt bifogar din token i Authorization-headern på varje anrop:
Authorization: Bearer <din-token>

Behörighet: Appen dekodar token (via jwt-decode) för att veta om användaren är en djurägare (ROLE_OWNER), veterinär (ROLE_VET) eller admin(ROLE_ADMIN).


### Projektstruktur

**src/components/:** Återanvändbara UI-komponenter (Layout, knappar, formulär).

**src/pages/:** Huvudvyer (Dashboard, Login, PetDetails).

**src/services/api.js:** All kommunikation med backend (Axios-instans).

**App.jsx:** Appens huvudkomponent som styr vy-hantering och autentiseringsstatus.

### Att tänka på vid utveckling
Backend-anslutning: Frontenden förväntar sig att backenden körs på http://localhost:8080/api.

Nya bibliotek: Om du installerar ett nytt bibliotek (npm install <namn>), se till att även pusha package.json och package-lock.json.

Git: Mappen node_modules är ignorerad via .gitignore och ska aldrig laddas upp