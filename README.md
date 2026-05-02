# LaboTrack — Étape 2

## Schéma d'architecture

*(voir schéma joint — généré avec napkin.ai)*

Le flux complet : **Frontend** (nginx) → **service1** (enregistrement) → **service2** (analyse 2s) → **service3** (validation + persistance H2). Tous les appels inter-services transitent par le mesh **Linkerd** (mTLS, observabilité, timeout).

---

## Dockerfiles multistage

| Service | Dockerfile |
|---|---|
| service1 | `service1_api/Dockerfile` |
| service2 | `service2_api/Dockerfile` |
| service3 | `service3_api/Dockerfile` |
| frontend | `frontend/Dockerfile` |

Chaque Dockerfile backend utilise deux étapes : build Maven (`maven:3.9-eclipse-temurin-21`) puis runtime JRE Alpine (`eclipse-temurin:21-jre-alpine`). Le frontend utilise Alpine pour les fichiers statiques puis `nginx:1.27-alpine`.

---

## Commandes de build et push

```bash
# Cibler le daemon Docker de Minikube
eval $(minikube docker-env)

# Build
docker build -t labotrack/service1:1.0 ./service1_api
docker build -t labotrack/service2:1.0 ./service2_api
docker build -t labotrack/service3:1.0 ./service3_api
docker build -t labotrack/frontend:1.0 ./frontend

# Push (si registry distant configuré)
docker push labotrack/service1:1.0
docker push labotrack/service2:1.0
docker push labotrack/service3:1.0
docker push labotrack/frontend:1.0
```

> En local avec Minikube, le push n'est pas nécessaire : les images sont buildées directement dans le daemon de Minikube (`imagePullPolicy: Never` dans les manifests).

---

## Manifests Kubernetes

Tous les manifests sont dans le dossier `k8s/` :

| Fichier | Contenu |
|---|---|
| `namespace.yml` | Namespace `labotrack` avec injection Linkerd automatique |
| `service1.yml` | Deployment + Service (port 9000) |
| `service2.yml` | Deployment + Service (port 9001) + ServiceProfile Linkerd (timeout 15s) |
| `service3.yml` | Deployment + Service (port 9002) + PersistentVolumeClaim 256Mi |
| `frontend.yml` | Deployment + Service NodePort 30080 |

---

## RunBook

Le fichier `runbook.sh` contient toutes les commandes pour lancer le projet complet.

### Test local

```bash
docker compose down -v
docker system prune -a -f      # libérer l'espace avant Minikube
docker compose up --build -d
```

→ http://localhost:8080

### Déploiement Minikube

```bash
./runbook.sh minikube-start
./runbook.sh linkerd-install
./runbook.sh build
./runbook.sh deploy
./runbook.sh open              # affiche l'URL (http://<ip>:30080)
```

Ou en une seule commande :

```bash
./runbook.sh all
```

### Vérification Linkerd

```bash
./runbook.sh check
```
