# Dockerisation - Backend Gestion Magasin

## Construction de l'image

```bash
docker build -t backend-magasin:latest .
```

## Lancer en local (compose)

```bash
# Copier .env.example -> .env puis modifier les valeurs
cp .env.example .env

# Lancer les services (MySQL + app)
docker-compose up --build
```

## En production

- Gérer les secrets via un gestionnaire (Vault, Docker secrets) ou variables d'environnement CI/CD
- Utiliser `pm2` à l'intérieur du conteneur si besoin, ou exécuter le conteneur via un orchestrateur (Docker Swarm / Kubernetes)

## Notes
- Le `docker-compose.yml` expose MySQL sur le port 3306 pour faciliter le développement — restreindre en production.
- `app` utilise le volume `.:/usr/src/app` pour rechargement en développement. En production, retirer le volume et exécuter la build statique.
