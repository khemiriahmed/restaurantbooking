# Restaurant Booking — Frontend

Application Angular 21 (standalone, zoneless, signaux) du projet **Restaurant Booking**.

Voir le [README principal](../README.md) pour le démarrage complet (backend + base de données).

## Développement

```bash
npm install
npm start        # http://localhost:4200
```

L'URL de l'API est configurée dans `src/environments/environment.ts` (`http://localhost:8080/api`).
En production, le build utilise `environment.production.ts` (`/api`) et le reverse-proxy nginx de l'image Docker.

## Build de production

```bash
npm run build    # sortie : dist/restaurant-booking-frontend
```