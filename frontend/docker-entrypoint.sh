#!/bin/sh
set -e

: "${SERVICE1_URL:?SERVICE1_URL est requis}"
: "${SERVICE3_URL:?SERVICE3_URL est requis}"

export SERVICE1_URL SERVICE3_URL
envsubst '${SERVICE1_URL} ${SERVICE3_URL}' \
    < /etc/nginx/templates/default.conf.template \
    > /etc/nginx/conf.d/default.conf

exec nginx -g "daemon off;"
