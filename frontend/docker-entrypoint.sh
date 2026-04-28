#!/bin/sh
set -e

SERVICE1_URL="${SERVICE1_URL:-}"
SERVICE3_URL="${SERVICE3_URL:-}"
MOCK_MODE="${MOCK_MODE:-false}"

# Si une URL manque, on bascule à l'utilisation de mock data
if [ -z "$SERVICE1_URL" ] || [ -z "$SERVICE3_URL" ]; then
    echo "[labotrack] Aucune URL de service fournie — démarrage en mode mock."
    MOCK_MODE="true"
fi

# Sinon config.js configuré à partir des variables d'environnement.
cat > /usr/share/nginx/html/config.js <<EOF
window.LABOTRACK_CONFIG = { mockMode: ${MOCK_MODE} };
EOF

export SERVICE1_URL SERVICE3_URL
envsubst '${SERVICE1_URL} ${SERVICE3_URL}' \
    < /etc/nginx/templates/default.conf.template \
    > /etc/nginx/conf.d/default.conf

echo "[labotrack] Démarrage nginx (mockMode=${MOCK_MODE})."
exec nginx -g "daemon off;"
