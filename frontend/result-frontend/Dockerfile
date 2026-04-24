# ── Étape 1 : packaging des sources statiques ────────────────────────────────
# On utilise alpine comme étape intermédiaire pour isoler le contexte de build.
# Si on ajoute un bundler (vite, esbuild…) plus tard, il s'insère ici sans
# modifier l'étape de runtime.
FROM alpine:3.20 AS builder

WORKDIR /app
COPY src/ .


# ── Étape 2 : image de production nginx ──────────────────────────────────────
FROM nginx:1.27-alpine

# Fichiers statiques
COPY --from=builder /app /usr/share/nginx/html

# Template nginx — envsubst le peuple au démarrage via docker-entrypoint.sh
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

# Supprime la conf nginx par défaut pour éviter les conflits
RUN rm -f /etc/nginx/conf.d/default.conf

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
