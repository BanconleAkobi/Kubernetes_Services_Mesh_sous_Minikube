# LaboTrack - Étape 2

## Lancer en local

```bash
docker compose up --build -d
```

→ http://localhost:8080

## Déployer sur Minikube

Libérer l'espace Docker avant de lancer Minikube :

```bash
docker compose down -v
docker system prune -a -f
```

```bash
./runbook.sh minikube-start
./runbook.sh linkerd-install
./runbook.sh build
./runbook.sh deploy
./runbook.sh open        # affiche l'URL
```

## Commandes utiles

```bash
./runbook.sh check       # état du mesh Linkerd
./runbook.sh compose-down
docker compose logs -f
```
