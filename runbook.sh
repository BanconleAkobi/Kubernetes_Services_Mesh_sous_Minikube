#!/bin/bash
set -e

REGISTRY="labotrack"
TAG="1.0"

build_images() {
    echo ">>> Connexion au daemon Docker de Minikube..."
    eval "$(minikube docker-env)"

    echo ">>> Build service1..."
    docker build -t $REGISTRY/service1:$TAG ./service1_api

    echo ">>> Build service2..."
    docker build -t $REGISTRY/service2:$TAG ./service2_api

    echo ">>> Build service3..."
    docker build -t $REGISTRY/service3:$TAG ./service3_api

    echo ">>> Build frontend..."
    docker build -t $REGISTRY/frontend:$TAG ./frontend

    echo ">>> Images disponibles dans Minikube :"
    docker images | grep $REGISTRY
}

compose_up() {
    echo ">>> Démarrage docker-compose..."
    docker compose up -d
    echo ">>> Frontend accessible sur http://localhost:8080"
    echo "    Attendre ~30s le démarrage des services Java."
}

compose_down() {
    docker compose down -v
}

minikube_start() {
    echo ">>> Démarrage Minikube..."
    minikube start --cpus=4 --memory=4096 --force
}

linkerd_install() {
    echo ">>> Vérification pré-installation Linkerd..."
    linkerd check --pre

    echo ">>> Installation des Gateway API CRDs..."
    kubectl apply --server-side -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.4.0/standard-install.yaml

    echo ">>> Installation des CRDs Linkerd..."
    linkerd install --crds | kubectl apply -f -

    echo ">>> Installation du control plane Linkerd..."
    linkerd install --set proxyInit.runAsRoot=true | kubectl apply -f -

    echo ">>> Attente du control plane..."
    linkerd check

    echo ">>> Installation de Linkerd Viz..."
    linkerd viz install | kubectl apply -f -
    linkerd viz check
}

deploy() {
    echo ">>> Namespace labotrack..."
    kubectl apply -f k8s/namespace.yml

    echo ">>> Déploiement des services..."
    kubectl apply -f k8s/service3.yml
    kubectl apply -f k8s/service2.yml
    kubectl apply -f k8s/service1.yml
    kubectl apply -f k8s/frontend.yml

    echo ">>> Attente des pods..."
    kubectl rollout status deployment/service3  -n labotrack --timeout=120s
    kubectl rollout status deployment/service2  -n labotrack --timeout=120s
    kubectl rollout status deployment/service1  -n labotrack --timeout=120s
    kubectl rollout status deployment/frontend  -n labotrack --timeout=60s

    echo ">>> Pods :"
    kubectl get pods -n labotrack
}

open_app() {
    echo ">>> URL du frontend :"
    minikube service frontend -n labotrack --url
}

check_linkerd() {
    linkerd check
    echo ""
    linkerd viz stat deploy -n labotrack
    echo ""
    echo ">>> Dashboard Linkerd :"
    linkerd viz dashboard &
}

case "$1" in
    build)            build_images ;;
    compose-up)       compose_up ;;
    compose-down)     compose_down ;;
    minikube-start)   minikube_start ;;
    linkerd-install)  linkerd_install ;;
    deploy)           deploy ;;
    open)             open_app ;;
    check)            check_linkerd ;;
    all)
        minikube_start
        linkerd_install
        build_images
        deploy
        open_app
        ;;
    *)
        echo "Usage: $0 {build|compose-up|compose-down|minikube-start|linkerd-install|deploy|open|check|all}"
        echo ""
        echo "  build            — build les 4 images dans le daemon Minikube"
        echo "  compose-up       — test local avec docker-compose"
        echo "  compose-down     — arrête docker-compose"
        echo "  minikube-start   — démarre Minikube"
        echo "  linkerd-install  — installe Linkerd + Viz"
        echo "  deploy           — déploie l'application dans Kubernetes"
        echo "  open             — affiche l'URL du frontend"
        echo "  check            — vérifie Linkerd et affiche les stats"
        echo "  all              — enchaîne tout (minikube → linkerd → build → deploy)"
        ;;
esac
