#!/bin/bash
set -e

REGISTRY="labotrack"
TAG="1.0"

# ─────────────────────────────────────────────────────────────────────────────
# ÉTAPE 1 — Build des images Docker
# ─────────────────────────────────────────────────────────────────────────────

build_images() {
    echo ">>> Build service1..."
    docker build -t $REGISTRY/service1:$TAG ./service1_api

    echo ">>> Build service2..."
    docker build -t $REGISTRY/service2:$TAG ./service2_api

    echo ">>> Build service3..."
    docker build -t $REGISTRY/service3:$TAG ./service3_api

    echo ">>> Build frontend..."
    docker build -t $REGISTRY/frontend:$TAG ./frontend

    echo ">>> Images construites :"
    docker images | grep $REGISTRY
}

# ─────────────────────────────────────────────────────────────────────────────
# ÉTAPE 2 — Test local avec docker-compose
# ─────────────────────────────────────────────────────────────────────────────

compose_up() {
    echo ">>> Démarrage docker-compose..."
    docker compose up -d
    echo ">>> Frontend accessible sur http://localhost:8080"
    echo "    Attendre ~30s le démarrage des services Java."
}

compose_down() {
    docker compose down -v
}

# ─────────────────────────────────────────────────────────────────────────────
# ÉTAPE 3 — Préparation de Minikube
# ─────────────────────────────────────────────────────────────────────────────

minikube_start() {
    echo ">>> Démarrage Minikube..."
    minikube start --cpus=4 --memory=4096

    # Les images buildées localement doivent être chargées dans Minikube
    echo ">>> Chargement des images dans Minikube..."
    minikube image load $REGISTRY/service1:$TAG
    minikube image load $REGISTRY/service2:$TAG
    minikube image load $REGISTRY/service3:$TAG
    minikube image load $REGISTRY/frontend:$TAG
}

# ─────────────────────────────────────────────────────────────────────────────
# ÉTAPE 4 — Installation de Linkerd
# ─────────────────────────────────────────────────────────────────────────────

linkerd_install() {
    echo ">>> Vérification pré-installation Linkerd..."
    linkerd check --pre

    echo ">>> Installation des CRDs Linkerd..."
    linkerd install --crds | kubectl apply -f -

    echo ">>> Installation du control plane Linkerd..."
    linkerd install | kubectl apply -f -

    echo ">>> Attente de la disponibilité du control plane..."
    linkerd check

    echo ">>> Installation de Linkerd Viz (observabilité)..."
    linkerd viz install | kubectl apply -f -
    linkerd viz check
}

# ─────────────────────────────────────────────────────────────────────────────
# ÉTAPE 5 — Déploiement de l'application
# ─────────────────────────────────────────────────────────────────────────────

deploy() {
    echo ">>> Création du namespace labotrack (injection Linkerd activée)..."
    kubectl apply -f k8s/namespace.yml

    echo ">>> Déploiement des services..."
    kubectl apply -f k8s/service3.yml
    kubectl apply -f k8s/service2.yml
    kubectl apply -f k8s/service1.yml
    kubectl apply -f k8s/frontend.yml

    echo ">>> Attente de la disponibilité des pods..."
    kubectl rollout status deployment/service1 -n labotrack --timeout=120s
    kubectl rollout status deployment/service2 -n labotrack --timeout=120s
    kubectl rollout status deployment/service3 -n labotrack --timeout=120s
    kubectl rollout status deployment/frontend -n labotrack --timeout=60s

    echo ">>> Pods déployés :"
    kubectl get pods -n labotrack
}

# ─────────────────────────────────────────────────────────────────────────────
# ÉTAPE 6 — Accès et vérification
# ─────────────────────────────────────────────────────────────────────────────

open_app() {
    echo ">>> URL du frontend :"
    minikube service frontend -n labotrack --url
}

check_linkerd() {
    echo ">>> Vérification du mesh Linkerd..."
    linkerd check
    echo ""
    echo ">>> Proxies injectés dans labotrack :"
    linkerd viz stat deploy -n labotrack
    echo ""
    echo ">>> Dashboard Linkerd (ouvrir dans un navigateur) :"
    linkerd viz dashboard &
}

# ─────────────────────────────────────────────────────────────────────────────
# Commandes disponibles
# ─────────────────────────────────────────────────────────────────────────────

case "$1" in
    build)          build_images ;;
    compose-up)     compose_up ;;
    compose-down)   compose_down ;;
    minikube-start) minikube_start ;;
    linkerd-install) linkerd_install ;;
    deploy)         deploy ;;
    open)           open_app ;;
    check)          check_linkerd ;;
    all)
        build_images
        minikube_start
        linkerd_install
        deploy
        open_app
        ;;
    *)
        echo "Usage: $0 {build|compose-up|compose-down|minikube-start|linkerd-install|deploy|open|check|all}"
        echo ""
        echo "  build            — construit les 4 images Docker"
        echo "  compose-up       — test local avec docker-compose"
        echo "  compose-down     — arrête docker-compose"
        echo "  minikube-start   — démarre Minikube et charge les images"
        echo "  linkerd-install  — installe Linkerd sur Minikube"
        echo "  deploy           — déploie l'application dans Kubernetes"
        echo "  open             — affiche l'URL du frontend"
        echo "  check            — vérifie Linkerd et affiche les stats"
        echo "  all              — enchaîne tout (build → minikube → linkerd → deploy)"
        ;;
esac
