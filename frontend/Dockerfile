FROM nginx:alpine

# Install curl for Healthcheck
RUN apk add --no-cache curl

WORKDIR /usr/share/nginx/html

# Clean default
RUN rm -rf ./*

# Copy static assets
COPY . .

# COPY CUSTOM NGINX CONFIGURATION (Vital for Reverse Proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Permissions
RUN chown -R nginx:nginx /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD curl --fail http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
