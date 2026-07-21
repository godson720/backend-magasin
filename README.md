# Backend Gestion Magasin 🏪

API REST pour la gestion d'un magasin en ligne avec authentification JWT.

## 📋 Fonctionnalités

- **Authentification** : Login sécurisé avec JWT
- **Gestion des Produits** : CRUD complet
- **Gestion des Ventes** : Enregistrement des transactions
- **Tableau de Bord** : Statistiques et indicateurs
- **Contrôle d'Accès** : Rôles (Admin, Gestionnaire Stock, Caissier)

## 🚀 Installation

1. Cloner le projet
```bash
git clone <repo>
cd backend-magasin
```

2. Installer les dépendances
```bash
npm install
```

3. Configurer les variables d'environnement
- Créer un fichier `.env` à la racine
- Utiliser `.env.example` comme référence

4. Configurer la base de données MySQL
- Créer la base de données `gestion_magasin`
- Importer le schéma SQL

## 📝 Variables d'environnement (.env)

```
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=votre_mot_de_passe
DB_NAME=gestion_magasin
JWT_SECRET=votre_secret_complexe
JWT_EXPIRES_IN=24h
PORT=3000
```

## 🔨 Scripts

```bash
npm start      # Démarrer le serveur en production
npm run dev    # Démarrer en mode développement (nodemon)
```

## 📚 Routes API

### Authentification
- `POST /auth/login` - Connexion
- `GET /auth/utilisateurs` - Lister les utilisateurs (Admin)
- `POST /auth/utilisateurs` - Créer un utilisateur (Admin)
- `DELETE /auth/utilisateurs/:id` - Supprimer un utilisateur (Admin)

### Produits
- `GET /produits` - Lister tous les produits
- `GET /produits/:id` - Détail d'un produit
- `POST /produits` - Ajouter un produit (Admin, Gestionnaire Stock)
- `PUT /produits/:id` - Modifier un produit (Admin, Gestionnaire Stock)
- `DELETE /produits/:id` - Supprimer un produit (Admin)

### Ventes
- `POST /ventes` - Enregistrer une vente (Admin, Caissier)
- `GET /ventes` - Lister les ventes (Admin)

### Dashboard
- `GET /dashboard` - Statistiques (Admin)

## 🔐 Rôles et Permissions

| Rôle | Permissions |
|------|------------|
| Admin | Accès complet |
| Gestionnaire Stock | Gestion des produits |
| Caissier | Enregistrement des ventes |

## 📦 Dépendances

- **express** - Framework web
- **mysql2/promise** - Client MySQL asynchrone
- **jsonwebtoken** - JWT pour l'authentification
- **bcryptjs** - Hachage des mots de passe
- **cors** - CORS middleware
- **dotenv** - Variables d'environnement

## 🛠️ Développement

- **nodemon** - Rechargement automatique en développement

## 📄 Licence

ISC

## ✍️ Auteur

Projet de gestion de magasin
