#!/bin/sh
set -e

SAMPLE_API_URL="${SAMPLE_API_URL:-}"
ANALYSIS_API_URL="${ANALYSIS_API_URL:-}"
MOCK_MODE="${MOCK_MODE:-false}"

# Si une URL manque, on bascule automatiquement en mode mock
# pour éviter de démarrer avec une configuration incomplète.
if [ -z "$SAMPLE_API_URL" ] || [ -z "$ANALYSIS_API_URL" ]; then
    echo "[labotrack] Aucune URL d'API fournie — démarrage en mode mock."
    MOCK_MODE="true"
fi

# Génère config.js à partir des variables d'environnement.
cat > /usr/share/nginx/html/config.js <<EOF
window.LABOTRACK_CONFIG = { mockMode: ${MOCK_MODE} };
EOF

# Génère la configuration nginx en substituant les variables dans le template.
export SAMPLE_API_URL ANALYSIS_API_URL
envsubst '${SAMPLE_API_URL} ${ANALYSIS_API_URL}' \
    < /etc/nginx/templates/default.conf.template \
    > /etc/nginx/conf.d/default.conf

echo "[labotrack] Démarrage nginx (mockMode=${MOCK_MODE})."
exec nginx -g "daemon off;"
