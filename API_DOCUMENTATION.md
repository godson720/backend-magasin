# Documentation complète des Endpoints

## Base URL
```
http://localhost:3000
```

## Headers requis pour les requêtes authentifiées
```json
{
  "Authorization": "Bearer YOUR_JWT_TOKEN",
  "Content-Type": "application/json"
}
```

---

## 🔐 AUTHENTIFICATION

### 1. Login
**POST** `/auth/login`

Authentifier un utilisateur et obtenir un JWT token.

**Request Body:**
```json
{
  "email": "admin@magasin.com",
  "mot_de_passe": "Admin1234!"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "utilisateur": {
    "id": 1,
    "nom": "Admin",
    "email": "admin@magasin.com",
    "role": "admin"
  }
}
```

**Erreurs:**
- 400: Email et mot de passe requis
- 401: Identifiants incorrects

---

### 2. Lister les utilisateurs
**GET** `/auth/utilisateurs`

Récupérer la liste de tous les utilisateurs (Admin uniquement).

**Headers:** Authorization (Token requis)

**Response (200):**
```json
[
  {
    "id": 1,
    "nom": "Admin",
    "email": "admin@magasin.com",
    "role": "admin",
    "created_at": "2024-01-15T10:30:00Z"
  }
]
```

**Erreurs:**
- 401: Token manquant
- 403: Accès refusé (non-admin)

---

### 3. Créer un utilisateur
**POST** `/auth/utilisateurs`

Créer un nouvel utilisateur (Admin uniquement).

**Request Body:**
```json
{
  "nom": "Caissier 1",
  "email": "caissier1@magasin.com",
  "mot_de_passe": "SecurePassword123!",
  "role": "caissier"
}
```

**Response (201):**
```json
{
  "message": "Utilisateur créé",
  "id": 2
}
```

**Erreurs:**
- 400: Tous les champs sont requis
- 409: Email déjà utilisé
- 403: Rôle insuffisant

---

### 4. Supprimer un utilisateur
**DELETE** `/auth/utilisateurs/:id`

Supprimer un utilisateur existant (Admin uniquement).

**Response (200):**
```json
{
  "message": "Utilisateur supprimé"
}
```

**Erreurs:**
- 403: Rôle insuffisant
- 404: Utilisateur non trouvé

---

## 📦 PRODUITS

### 5. Lister les produits
**GET** `/produits`

Récupérer tous les produits avec filtrage optionnel.

**Query Parameters:**
- `search` (optionnel): Rechercher par nom
- `categorie` (optionnel): Filtrer par catégorie

**Example:**
```
GET /produits?search=Ordinateur&categorie=Électronique
```

**Headers:** Authorization (Token requis)

**Response (200):**
```json
[
  {
    "id": 1,
    "nom": "Ordinateur Portable",
    "prix": 899.99,
    "quantite": 15,
    "categorie": "Électronique",
    "created_at": "2024-01-15T10:30:00Z",
    "updated_at": "2024-01-15T10:30:00Z"
  }
]
```

**Erreurs:**
- 401: Token manquant

---

### 6. Obtenir un produit
**GET** `/produits/:id`

Récupérer les détails d'un produit spécifique.

**Headers:** Authorization (Token requis)

**Response (200):**
```json
{
  "id": 1,
  "nom": "Ordinateur Portable",
  "prix": 899.99,
  "quantite": 15,
  "categorie": "Électronique",
  "created_at": "2024-01-15T10:30:00Z",
  "updated_at": "2024-01-15T10:30:00Z"
}
```

**Erreurs:**
- 401: Token manquant
- 404: Produit introuvable

---

### 7. Ajouter un produit
**POST** `/produits`

Créer un nouveau produit (Admin, Gestionnaire Stock).

**Request Body:**
```json
{
  "nom": "Smartphone",
  "prix": 499.99,
  "quantite": 30,
  "categorie": "Électronique"
}
```

**Response (201):**
```json
{
  "message": "Produit ajouté",
  "id": 6
}
```

**Erreurs:**
- 400: Tous les champs sont requis
- 403: Rôle insuffisant

---

### 8. Modifier un produit
**PUT** `/produits/:id`

Mettre à jour un produit existant (Admin, Gestionnaire Stock).

**Request Body:**
```json
{
  "nom": "Ordinateur Portable Pro",
  "prix": 1099.99,
  "quantite": 20,
  "categorie": "Électronique"
}
```

**Response (200):**
```json
{
  "message": "Produit modifié"
}
```

**Erreurs:**
- 400: Champs invalides
- 403: Rôle insuffisant
- 404: Produit introuvable

---

### 9. Supprimer un produit
**DELETE** `/produits/:id`

Supprimer un produit (Admin uniquement).

**Response (200):**
```json
{
  "message": "Produit supprimé"
}
```

**Erreurs:**
- 403: Rôle insuffisant
- 404: Produit introuvable

---

## 💰 VENTES

### 10. Enregistrer une vente
**POST** `/ventes`

Créer une nouvelle vente (Admin, Caissier).

**Request Body:**
```json
{
  "produit_id": 1,
  "quantite": 2
}
```

**Response (201):**
```json
{
  "message": "Vente enregistrée",
  "id": 1,
  "montant": 1799.98,
  "produit": "Ordinateur Portable"
}
```

**Erreurs:**
- 400: produit_id et quantite (> 0) requis
- 400: Stock insuffisant
- 404: Produit introuvable
- 403: Rôle insuffisant

---

### 11. Lister les ventes
**GET** `/ventes`

Récupérer toutes les ventes (Admin uniquement).

**Query Parameters:**
- `date` (optionnel): Filtrer par date (format: YYYY-MM-DD)

**Example:**
```
GET /ventes?date=2024-01-15
```

**Headers:** Authorization (Token requis)

**Response (200):**
```json
[
  {
    "id": 1,
    "quantite": 2,
    "montant": 1799.98,
    "date_vente": "2024-01-15T14:30:00Z",
    "produit": "Ordinateur Portable",
    "caissier": "Admin"
  }
]
```

**Erreurs:**
- 401: Token manquant
- 403: Rôle insuffisant

---

## 📊 DASHBOARD

### 12. Statistiques
**GET** `/dashboard`

Obtenir les statistiques du magasin (Admin uniquement).

**Headers:** Authorization (Token requis)

**Response (200):**
```json
{
  "total_produits": 5,
  "ventes_jour": 10,
  "chiffre_affaires": 15999.90,
  "nb_ruptures": 2,
  "produits_rupture": [
    {
      "id": 3,
      "nom": "Chaise Gaming",
      "quantite": 2
    }
  ],
  "top_produits": [
    {
      "nom": "Ordinateur Portable",
      "total_vendu": 15
    }
  ]
}
```

**Erreurs:**
- 401: Token manquant
- 403: Rôle insuffisant

---

## 🔐 Rôles et Permissions

| Endpoint | Admin | Gestionnaire Stock | Caissier |
|----------|-------|-------------------|----------|
| GET /produits | ✅ | ✅ | ✅ |
| POST /produits | ✅ | ✅ | ❌ |
| PUT /produits | ✅ | ✅ | ❌ |
| DELETE /produits | ✅ | ❌ | ❌ |
| GET /auth/utilisateurs | ✅ | ❌ | ❌ |
| POST /auth/utilisateurs | ✅ | ❌ | ❌ |
| POST /ventes | ✅ | ❌ | ✅ |
| GET /ventes | ✅ | ❌ | ❌ |
| GET /dashboard | ✅ | ❌ | ❌ |

---

## 🛠️ Codes d'erreur HTTP

| Code | Signification |
|------|---------------|
| 200 | Requête réussie |
| 201 | Ressource créée |
| 400 | Mauvaise requête (données invalides) |
| 401 | Non authentifié (token manquant) |
| 403 | Non autorisé (rôle insuffisant) |
| 404 | Ressource non trouvée |
| 409 | Conflit (ressource existe déjà) |
| 500 | Erreur serveur |

---

## 📝 Notes importantes

1. **Authentification**: Tous les endpoints (sauf /auth/login) nécessitent un token JWT valide
2. **Token Format**: `Authorization: Bearer YOUR_TOKEN_HERE`
3. **Token Expiration**: Par défaut, 24 heures
4. **Stock**: La quantité d'un produit diminue automatiquement lors d'une vente
5. **Dates**: Toutes les dates sont en UTC
6. **Pagination**: À implémenter si besoin

---

## 🧪 Exemple complet (cURL)

### 1. Se connecter
```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@magasin.com","mot_de_passe":"Admin1234!"}'
```

### 2. Utiliser le token pour un appel
```bash
TOKEN="votre_token_ici"
curl -X GET http://localhost:3000/produits \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Ajouter un produit
```bash
TOKEN="votre_token_ici"
curl -X POST http://localhost:3000/produits \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Nouvel Article","prix":99.99,"quantite":50,"categorie":"Électronique"}'
```
