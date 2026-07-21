# Guide d'Installation - Backend Gestion Magasin

## 📋 Prérequis

- **Node.js** v14 ou supérieur
- **npm** v6 ou supérieur
- **MySQL** v5.7 ou supérieur

## 🔧 Étapes d'installation

### 1. Cloner le projet

```bash
git clone <votre-repo>
cd backend-magasin
```

### 2. Installer les dépendances

```bash
npm install
```

### 3. Configurer la base de données MySQL

#### Option A : Ligne de commande MySQL

```bash
mysql -u root -p < schema.sql
```

#### Option B : Interface graphique (phpMyAdmin)

1. Créer une base de données nommée `gestion_magasin`
2. Importer le fichier `schema.sql` dans phpMyAdmin

#### Option C : MySQL Workbench

1. Ouvrir MySQL Workbench
2. Exécuter le script `schema.sql`

### 4. Configurer les variables d'environnement

1. Dupliquer le fichier `.env.example` en `.env`

```bash
cp .env.example .env
```

2. Modifier le fichier `.env` avec vos paramètres MySQL :

```
DB_HOST=localhost
DB_PORT=3306
DB_USER=votre_utilisateur
DB_PASSWORD=votre_mot_de_passe
DB_NAME=gestion_magasin
JWT_SECRET=votre_secret_long_et_complexe
JWT_EXPIRES_IN=24h
PORT=3000
```

### 5. Démarrer le serveur

#### Mode développement (avec rechargement automatique)

```bash
npm run dev
```

#### Mode production

```bash
npm start
```

Le serveur démarrera sur `http://localhost:3000`

## 🧪 Tester l'API

### Données de connexion par défaut

```json
{
  "email": "admin@magasin.com",
  "mot_de_passe": "Admin1234!"
}
```

### Tester avec Postman ou cURL

#### 1. Se connecter

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@magasin.com",
    "mot_de_passe": "Admin1234!"
  }'
```

#### 2. Utiliser le token

```bash
curl -X GET http://localhost:3000/produits \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 🐛 Dépannage

### Erreur : "connect ECONNREFUSED"

- Vérifier que MySQL est en cours d'exécution
- Vérifier les paramètres dans `.env`

### Erreur : "Access denied for user"

- Vérifier le nom d'utilisateur et le mot de passe MySQL
- Vérifier les permissions de l'utilisateur MySQL

### Erreur : "database does not exist"

- Exécuter le fichier `schema.sql` pour créer la base de données

## 📦 Structure des fichiers

```
backend-magasin/
├── config/
│   └── db.js              # Configuration de la base de données
├── middleware/
│   └── auth.js            # Middleware d'authentification
├── routes/
│   ├── authRoutes.js      # Routes d'authentification
│   ├── produitRoutes.js   # Routes des produits
│   ├── venteRoutes.js     # Routes des ventes
│   └── dashboardRoutes.js # Routes du tableau de bord
├── .env                   # Variables d'environnement (à créer)
├── .env.example           # Exemple de variables d'environnement
├── .gitignore             # Fichiers à ignorer
├── package.json           # Dépendances du projet
├── server.js              # Point d'entrée du serveur
├── schema.sql             # Schéma de la base de données
├── constants.js           # Constantes de l'application
├── validators.js          # Validateurs de données
├── errorHandler.js        # Gestionnaire d'erreurs
└── README.md              # Documentation générale
```

## ✅ Vérification de l'installation

Une fois le serveur démarré, vérifier que tout fonctionne :

1. Accéder à `http://localhost:3000/`
2. Vous devriez recevoir : `{ message: 'API Gestion Magasin en ligne' }`

## 📚 Documentation API

Consultez le `README.md` pour la liste complète des routes et endpoints.

## 🔐 Bonnes pratiques de sécurité

- ✅ Toujours utiliser un JWT_SECRET long et complexe
- ✅ Ne jamais commiter le fichier `.env` dans Git
- ✅ Mettre à jour les dépendances régulièrement : `npm update`
- ✅ Utiliser HTTPS en production
- ✅ Configurer CORS correctement en production

## 🆘 Support

En cas de problème, vérifier :
- Les logs du serveur
- Les erreurs MySQL
- Les variables d'environnement
