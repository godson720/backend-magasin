# Déploiement - Backend Gestion Magasin

## 🚀 Préparation pour la production

### 1. Vérifications avant déploiement

Avant de déployer en production, vérifier les points suivants:

```bash
# Vérifier qu'il n'y a pas de fichiers .env committé
git status | grep .env

# Vérifier les dépendances
npm audit

# Lancer les tests (si disponibles)
npm test

# Vérifier la construction
npm run build
```

### 2. Variables d'environnement en production

Créer un fichier `.env` avec des valeurs sécurisées :

```
NODE_ENV=production
DB_HOST=votre_serveur_mysql.com
DB_PORT=3306
DB_USER=utilisateur_db
DB_PASSWORD=motdepasse_securise_tres_long
DB_NAME=gestion_magasin
JWT_SECRET=un_secret_tres_long_et_complexe_genere_aleatoirement
JWT_EXPIRES_IN=24h
PORT=3000
CORS_ORIGIN=https://votre-domaine.com
LOG_LEVEL=info
```

### 3. Configuration du serveur

#### Utiliser PM2 pour la gestion des processus

```bash
# Installer PM2 globalement
npm install -g pm2

# Lancer l'application avec PM2
pm2 start server.js --name "backend-magasin" --instances 2

# Sauvegarder la configuration
pm2 save

# Démarrer au boot
pm2 startup
```

#### Utiliser nginx comme reverse proxy

```nginx
upstream backend {
    server localhost:3000;
    server localhost:3001;
}

server {
    listen 80;
    server_name votre-domaine.com;

    location / {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 4. SSL/HTTPS

#### Avec Let's Encrypt et Certbot

```bash
# Installer Certbot
sudo apt-get install certbot python3-certbot-nginx

# Générer un certificat
sudo certbot certonly --nginx -d votre-domaine.com

# Configurer nginx pour HTTPS
```

### 5. Monitoring et Logs

#### Rediriger les logs

```bash
pm2 logs backend-magasin > /var/log/backend-magasin.log
```

#### Configurer la rotation des logs

```bash
pm2 install pm2-logrotate
```

### 6. Sauvegardes de la base de données

#### Sauvegarder régulièrement

```bash
# Sauvegarder la base de données
mysqldump -u root -p gestion_magasin > backup_$(date +%Y%m%d).sql

# Restaurer une sauvegarde
mysql -u root -p gestion_magasin < backup_20240115.sql
```

#### Script de sauvegarde automatique

```bash
#!/bin/bash
BACKUP_DIR="/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="gestion_magasin"

mkdir -p $BACKUP_DIR
mysqldump -u root -p$DB_PASSWORD $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# Garder seulement les 7 derniers jours
find $BACKUP_DIR -type f -mtime +7 -delete
```

### 7. Optimisations de performance

#### Limiter la connexion de la base de données

```javascript
const config = {
  database: {
    connectionLimit: 20,
    enableKeepAlive: true,
    keepAliveInitialDelayMs: 0,
  }
};
```

#### Implémenter du caching

```javascript
// Avec Redis
const redis = require('redis');
const client = redis.createClient({
  host: 'localhost',
  port: 6379
});
```

#### Compression des réponses

```javascript
const compression = require('compression');
app.use(compression());
```

### 8. Sécurité

#### Headers de sécurité

```javascript
const helmet = require('helmet');
app.use(helmet());
```

#### Rate limiting

```javascript
const rateLimit = require('express-rate-limit');

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // Limite à 100 requêtes par fenêtre
});

app.use(limiter);
```

#### CORS sécurisé

```javascript
app.use(cors({
  origin: process.env.CORS_ORIGIN,
  credentials: true
}));
```

### 9. Monitoring et Alertes

#### Health check endpoint

```javascript
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});
```

#### Configurer des alertes

```bash
# Avec Sentry pour les erreurs
npm install @sentry/node

# Configuration Sentry
const Sentry = require("@sentry/node");
Sentry.init({ dsn: "https://xxxxx@xxxxx.ingest.sentry.io/xxxxx" });
app.use(Sentry.Handlers.errorHandler());
```

## 📊 Vérification après déploiement

1. ✅ Tester tous les endpoints
2. ✅ Vérifier les logs pour les erreurs
3. ✅ Contrôler l'utilisation du CPU et mémoire
4. ✅ Vérifier la connectivité à la base de données
5. ✅ Tester les sauvegardes

## 🔒 Checklist de sécurité en production

- [ ] JWT_SECRET est fort et unique
- [ ] Fichier .env n'est pas committé
- [ ] HTTPS est activé
- [ ] CORS est restreint au domaine autorisé
- [ ] Rate limiting est en place
- [ ] Les mots de passe sont hachés (bcryptjs)
- [ ] Les logs ne contiennent pas de données sensibles
- [ ] Les backups sont testés régulièrement
- [ ] Un plan de récupération existe
- [ ] Les dépendances sont à jour

## 📞 Support en production

- Monitoring : PM2, New Relic, DataDog
- Logs : ELK Stack, Splunk, CloudWatch
- Erreurs : Sentry, Rollbar
- APM : New Relic, DataDog
