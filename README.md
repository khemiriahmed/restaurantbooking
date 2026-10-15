#  Restaurant Booking — Réservation de restaurants

Application web **full-stack** de réservation de tables dans des restaurants :

- **Backend** : Spring Boot (Java 21), Spring Security + JWT, Spring Data JPA, PostgreSQL, OpenAPI (Swagger), tests d'intégration.
- **Frontend** : Angular 21 (standalone, zoneless, signaux), SCSS, FontAwesome, déployé derrière nginx.

---

##  Fonctionnalités

### Client
- Consultation de la liste des restaurants (recherche, filtre par ville et cuisine).
- Détail d'un restaurant : description, coordonnées, **horaires d'ouverture**, tables disponibles.
- Création d'une réservation (date, heures, nombre de personnes, choix de la table) avec **vérification des conflits** et des horaires.
- Gestion de ses réservations : consultation, filtrage par statut, annulation.

### Restaurateur (`RESTAURANT`)
- Espace de gestion (dashboard) : restaurants appartenant à l'utilisateur, réservations de ses restaurants, statuts.
- **Confirmer / refuser / terminer / annuler** les réservations.

### Administrateur (`ADMIN`)
- Accès complet : gestion de tous les restaurants et de toutes les réservations.
- Gestion des utilisateurs (`/api/users/**`).

### Sécurité
- Authentification JWT (headers `Authorization: Bearer <token>`).
- Rôles : `CLIENT`, `RESTAURANT`, `ADMIN`.
- **Scoping par propriété** : un restaurateur ne gère que ses restaurants ; un client n'accède qu'à ses réservations ; contrôle fin dans les services + règles HTTP.
- Handler JSON pour les erreurs `401` / `403` + `GlobalExceptionHandler` (validation, 404, 409…).
- CORS configurable par variable d'environnement.

---

##  Démarrage rapide

### Prérequis
- Java 21+ (testé avec JDK 26)
- Node.js 22+ et npm
- PostgreSQL 16 (ou Docker)

### 1. Base de données

```bash
docker compose up -d postgres
```

Ou avec un PostgreSQL local : créer la base `restaurant_booking`, utilisateur `restaurant_user` / `restaurant_password` (configurable dans `backend/src/main/resources/application.properties`).

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

L'API est disponible sur `http://localhost:8080`.

Documentation Swagger : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 3. Frontend (développement)

```bash
cd frontend
npm install
npm start          # http://localhost:4200
```

---

##  Tests

```bash
cd backend
./mvnw test
```

Les tests d'intégration couvrent le parcours complet : contrôle d'accès 401, lecture publique des restaurants, création d'une réservation par un client et confirmation par le restaurateur (base H2 en mémoire).

---

##  Démarrage complet avec Docker

```bash
docker compose up --build
```

| Service    | URL                        |
|------------|----------------------------|
| Frontend   | http://localhost:4200      |
| API        | http://localhost:8080/api  |
| Swagger    | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432             |

Le conteneur frontend (nginx) sert l'application et **redirige `/api/*` vers le backend**, ce qui évite tout problème de CORS en production.

---

##  Comptes de démonstration

> Créés automatiquement au démarrage par `DataInitializer` (idempotent).

| Rôle        | Email                          | Mot de passe |
|-------------|--------------------------------|--------------|
| **Admin**   | `admin@restaurant-booking.com` | `Admin@12345` |
| **Restaurateur** | `owner@restaurant-booking.com` | `Owner@12345` |
| **Client**  | `client@restaurant-booking.com` | `Client@12345` |

Trois restaurants de démonstration (horaires + tables) sont également créés.

---

##  Principaux endpoints

### Authentification
| Méthode | URL | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Inscription (rôle `CLIENT`) |
| `POST` | `/api/auth/login` | Connexion → JWT |

### Restaurants
| Méthode | URL | Accès |
|---|---|---|
| `GET` | `/api/restaurants` | Public |
| `GET` | `/api/restaurants/active` | Public |
| `GET` | `/api/restaurants/{id}` | Public |
| `GET` | `/api/restaurants/city/{city}` | Public |
| `GET` | `/api/restaurants/cuisine/{cuisine}` | Public |
| `GET` | `/api/restaurants/mine` | `RESTAURANT` / `ADMIN` |
| `GET` | `/api/restaurants/{id}/tables` | Public |
| `GET` | `/api/restaurants/{id}/tables/available` | Public |
| `GET` | `/api/restaurants/{id}/hours` | Public |
| `POST` | `/api/restaurants` | `RESTAURANT` / `ADMIN` (propriétaire affecté) |
| `PUT/PATCH/DELETE` | `/api/restaurants/{id}...` | Propriétaire ou `ADMIN` |
| `POST` | `/api/restaurants/{id}/tables` | Propriétaire ou `ADMIN` |

### Réservations (authentifié)
| Méthode | URL | Notes |
|---|---|---|
| `POST` | `/api/reservations` | L'utilisateur est déduit du token JWT |
| `GET` | `/api/reservations` | Scopé par rôle (toutes / ses restaurants / les siennes) |
| `GET` | `/api/reservations/{id}` | Propriétaire, restaurateur du resto, ou admin |
| `PATCH` | `/api/reservations/{id}/confirm` | Restaurateur ou admin |
| `PATCH` | `/api/reservations/{id}/reject` | Restaurateur ou admin |
| `PATCH` | `/api/reservations/{id}/complete` | Restaurateur ou admin |
| `PATCH` | `/api/reservations/{id}/cancel` | Client, restaurateur ou admin |
| `DELETE` | `/api/reservations/{id}` | Client propriétaire ou admin |

---

##  Configuration (variables d'environnement)

| Variable | Défaut | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Port de l'API |
| `DB_URL` | `jdbc:postgresql://localhost:5432/restaurant_booking` | URL JDBC |
| `DB_USERNAME` / `DB_PASSWORD` | `restaurant_user` / `restaurant_password` | Credentials PostgreSQL |
| `JWT_SECRET` | secret de développement | Secret de signature JWT (base64, ≥ 256 bits) |
| `JWT_EXPIRATION` | `86400000` | Durée de validité (ms) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200,http://localhost:4300` | Origines CORS autorisées |

