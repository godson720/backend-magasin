# FAQ - Backend Gestion Magasin

## 🔐 Authentification et Tokens

### Q: Comment réinitialiser le mot de passe admin?

**R:** Vous avez plusieurs options:

```bash
# Option 1: Utiliser hashMdp.js pour générer un hash
node hashMdp.js  # Entrez le nouveau mot de passe
# Copier le hash et mettre à jour manuellement en base de données

# Option 2: Exécuter directement en MySQL
UPDATE utilisateurs SET mot_de_passe = '$2a$10$...' WHERE email = 'admin@magasin.com';
```

### Q: Mon token expire trop rapidement, comment l'augmenter?

**R:** Modifiez la variable `JWT_EXPIRES_IN` dans `.env`:

```
JWT_EXPIRES_IN=72h  # 72 heures
JWT_EXPIRES_IN=7d   # 7 jours
JWT_EXPIRES_IN=30d  # 30 jours
```

### Q: Comment créer un nouveau rôle?

**R:** Les rôles actuels sont codés en dur. Pour en ajouter:

1. Modifier `schema.sql`:
```sql
role ENUM('admin', 'gestionnaire_stock', 'caissier', 'nouveau_role') DEFAULT 'caissier'
```

2. Mettre à jour `middleware/auth.js` si nécessaire
3. Créer les nouveaux endpoints avec les permissions appropriées

### Q: Que faire si le token est compromis?

**R:** Changez immédiatement `JWT_SECRET` dans `.env` et redémarrez le serveur. Tous les anciens tokens seront invalidés.

---

## 💾 Base de données

### Q: Comment importer le schéma SQL?

**R:** Plusieurs méthodes:

```bash
# Méthode 1: Ligne de commande
mysql -u root -p < schema.sql

# Méthode 2: Depuis MySQL CLI
mysql> source schema.sql;

# Méthode 3: phpMyAdmin
# 1. Ouvrir phpMyAdmin
# 2. Créer la base 'gestion_magasin'
# 3. Aller à l'onglet "Importer"
# 4. Sélectionner schema.sql
# 5. Cliquer "Exécuter"
```

### Q: Comment sauvegarder la base de données?

**R:**

```bash
# Sauvegarder
mysqldump -u root -p gestion_magasin > backup.sql

# Restaurer
mysql -u root -p gestion_magasin < backup.sql

# Sauvegarder avec date
mysqldump -u root -p gestion_magasin > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Q: Je reçois "Access denied for user"

**R:** Vérifiez vos identifiants dans `.env`:

```
DB_USER=root         # Nom d'utilisateur MySQL
DB_PASSWORD=xxxxx    # Mot de passe MySQL
DB_HOST=localhost    # Hôte MySQL
DB_PORT=3306         # Port MySQL (par défaut 3306)
```

### Q: Comment voir les produits créés?

**R:**

```sql
SELECT * FROM produits;
SELECT * FROM produits WHERE categorie = 'Électronique';
SELECT COUNT(*) AS total FROM produits WHERE quantite > 0;
```

### Q: Les ventes ne s'enregistrent pas

**R:** Vérifiez:

1. Le stock disponible: `SELECT quantite FROM produits WHERE id = 1;`
2. Les permissions: Vérifier le rôle de l'utilisateur
3. Les logs d'erreur: `npm run dev` affiche les erreurs
4. La base de données: Vérifier la connexion

---

## 🚀 Démarrage et arrêt

### Q: Comment démarrer le serveur en développement?

**R:**

```bash
# Mode développement (rechargement automatique)
npm run dev

# Mode production
npm start

# Avec PM2
pm2 start server.js --name "backend"
```

### Q: Le serveur refuse de démarrer

**R:** Vérifiez:

```bash
# Port déjà utilisé?
npm run dev
# Erreur: "listen EADDRINUSE: address already in use :::3000"

# Solution:
# Option 1: Changer le port dans .env
PORT=3001

# Option 2: Libérer le port (Windows PowerShell)
Get-Process -Id (Get-NetTCPConnection -LocalPort 3000).OwningProcess | Stop-Process -Force

# Option 2: Libérer le port (Linux/Mac)
lsof -i :3000
kill -9 <PID>
```

### Q: Nodemon ne redémarre pas les fichiers

**R:** Nodemon surveille les fichiers par défaut. Si ça ne fonctionne pas:

```bash
# Réinstaller nodemon
npm install -D nodemon

# Ou l'utiliser avec un delay
nodemon --delay 1000 server.js
```

---

## 🔗 Endpoints et requêtes

### Q: Comment tester une route avec cURL?

**R:**

```bash
# GET simple
curl http://localhost:3000/

# POST avec données
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@magasin.com","mot_de_passe":"Admin1234!"}'

# Avec authentification
curl -H "Authorization: Bearer TOKEN_HERE" \
  http://localhost:3000/produits
```

### Q: Qu'est-ce que ce message d'erreur signifie?

**R:** Erreurs courantes:

| Erreur | Signification | Solution |
|--------|---------------|----------|
| 400 Bad Request | Données invalides | Vérifier le JSON |
| 401 Unauthorized | Token manquant | Ajouter Authorization header |
| 403 Forbidden | Rôle insuffisant | Utiliser un compte autorisé |
| 404 Not Found | Ressource introuvable | Vérifier l'ID |
| 409 Conflict | Ressource existe déjà | Email déjà en base |
| 500 Server Error | Erreur serveur | Vérifier les logs |

### Q: Comment filtrer les produits?

**R:**

```bash
# Par nom
curl http://localhost:3000/produits?search=Ordinateur

# Par catégorie
curl http://localhost:3000/produits?categorie=Électronique

# Les deux
curl "http://localhost:3000/produits?search=Ordinateur&categorie=Électronique"
```

### Q: Comment trier les résultats?

**R:** Le tri n'est pas implémenté par défaut. Vous devez:

1. Modifier la route dans `routes/produitRoutes.js`
2. Ajouter un paramètre `sort`
3. Valider et appliquer le tri

---

## 📦 Dépendances

### Q: Qu'est-ce que bcryptjs?

**R:** Librairie pour hasher les mots de passe de manière sécurisée.

```javascript
const bcrypt = require('bcryptjs');

// Hasher
const hash = await bcrypt.hash('mon_mdp', 10);

// Vérifier
const valide = await bcrypt.compare('mon_mdp', hash);
```

### Q: Comment mettre à jour les dépendances?

**R:**

```bash
# Voir les mises à jour disponibles
npm outdated

# Mettre à jour tout
npm update

# Mettre à jour une dépendance spécifique
npm install express@latest

# Vérifier les vulnérabilités
npm audit

# Corriger les vulnérabilités automatiquement
npm audit fix
```

### Q: Une dépendance ne s'installe pas

**R:**

```bash
# Nettoyer le cache npm
npm cache clean --force

# Supprimer node_modules et package-lock.json
rm -rf node_modules package-lock.json

# Réinstaller
npm install
```

---

## 🐛 Bugs courants

### Q: "Cannot find module"

**R:**

```javascript
// Assurez-vous que le chemin est correct
const pool = require('../config/db');       // ✅ BON
// const pool = require('config/db');       // ❌ MAUVAIS (chemin relatif)

// Vérifier que le fichier existe
// Vérifier l'extension du fichier (.js)
```

### Q: "jwt malformed" ou "jwt expired"

**R:**

```javascript
// Vérifier le format du header
// "Authorization: Bearer VALIDE_TOKEN"

// Ne pas: "Authorization: VALIDE_TOKEN" (sans "Bearer")
// Ne pas: "Authorization: Bearer " (sans token)
```

### Q: Les mises à jour de produits ne s'appliquent pas

**R:** Vérifiez que vous utilisez l'ID correct:

```bash
# Avant de modifier, récupérer l'ID
curl http://localhost:3000/produits | grep -i "ordinateur"

# Utiliser le bon ID
curl -X PUT http://localhost:3000/produits/1 \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Nouveau nom","prix":999.99,"quantite":10,"categorie":"Électronique"}'
```

---

## 🚨 Erreurs de production

### Q: Le serveur est lent ou crash aléatoirement

**R:**

1. Augmenter la limite de connexions DB:
```javascript
connectionLimit: 20  // Augmenté de 10 à 20
```

2. Monitorer avec PM2:
```bash
pm2 monitor
```

3. Vérifier les logs:
```bash
pm2 logs backend-magasin
```

### Q: Les ventes disparaissent après un crash serveur

**R:** Les données doivent être dans MySQL, pas en mémoire. Vérifier:

```bash
# Vérifier que MySQL est en cours d'exécution
mysql -u root -p -e "SELECT 1"

# Vérifier les données
mysql -u root -p gestion_magasin -e "SELECT * FROM ventes;"
```

---

## 💡 Tips et astuces

### Tester rapidement l'API avec Postman

1. Importer `Postman_Collection.json`
2. Exécuter "Login" en premier
3. Le token s'enregistre automatiquement
4. Exécuter les autres endpoints

### Générer un JWT_SECRET fort

```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String([System.Security.Cryptography.RNGCryptoServiceProvider]::new().GetBytes(32))

# Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

### Debug les erreurs SQL

```javascript
// Ajouter des logs
console.log('Query:', query);
console.log('Params:', params);

// Voir la requête réelle
const formatted = pool.format(query, params);
console.log('Formatted:', formatted);
```

---

## 📞 Besoin d'aide supplémentaire?

1. Vérifier les fichiers de documentation:
   - README.md
   - API_DOCUMENTATION.md
   - INSTALLATION.md
   - BEST_PRACTICES.md

2. Vérifier les logs du serveur:
   - `npm run dev` affiche les erreurs

3. Consulter la base de code:
   - Les commentaires expliquent la logique
   - Les noms de fonction sont explicites

4. Consulter la documentation officielle:
   - [Express.js](https://expressjs.com)
   - [MySQL2](https://github.com/sidorares/node-mysql2)
   - [JWT](https://jwt.io)
   - [bcryptjs](https://www.npmjs.com/package/bcryptjs)
