# ---------------------------------------------------------------------------
# Étape 1 : compilation de l'application Angular
# ---------------------------------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

RUN npm run build

# ---------------------------------------------------------------------------
# Étape 2 : serveur nginx (fichiers statiques + reverse-proxy /api)
# ---------------------------------------------------------------------------
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist/restaurant-booking-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]