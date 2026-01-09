# --- ETAPA 1: BASE / DEV (Entorno de Desarrollo) ---
# Usamos la LTS 2026 como indicaste
FROM node:24.12-alpine AS dev

WORKDIR /app

# Esto es vital para evitar errores de "EACCES: permission denied"
RUN mkdir -p /app/node_modules && chown -R node:node /app/node_modules

ENV HOST=0.0.0.0
ENV PORT=5173

CMD ["npm", "run", "dev"]

# En modo 'dev' (target: dev), paramos aquí.
# Docker Compose montará el código y ejecutará 'npm run dev'.

# --- ETAPA 2: BUILDER (Compilación para Prod) ---
FROM node:24.12-alpine AS build-stage

WORKDIR /app
COPY package*.json ./
# Instalación limpia para producción
RUN npm ci

COPY . .
RUN npm run build

# --- ETAPA 3: PRODUCCIÓN (Nginx) ---
FROM nginx:alpine as production

# Instalamos curl para el Healthcheck
RUN apk add --no-cache curl

# Copiamos los artifacts compilados desde la etapa 'build-stage'
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Configuración de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Permisos
RUN chown -R nginx:nginx /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD curl --fail http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
