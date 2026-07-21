# Bonnes pratiques - Backend Gestion Magasin

## 📝 Guide de codage

### 1. Structure du code

```javascript
// ✅ BON
const express = require('express');
const { verifierToken, autoriserRoles } = require('../middleware/auth');
const pool = require('../config/db');

router.post('/endpoint', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    // Validation
    if (!req.body.data) {
      return res.status(400).json({ message: 'Données requises' });
    }
    
    // Logique métier
    const result = await pool.query('SELECT * FROM table WHERE id = ?', [req.body.data]);
    
    // Réponse
    res.json(result[0]);
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

// ❌ MAUVAIS
router.post('/endpoint', (req, res) => {
  pool.query('SELECT * FROM table WHERE id = ' + req.body.data, (err, result) => {
    if (err) throw err;  // Ne pas jeter l'erreur!
    res.json(result);
  });
});
```

### 2. Nommage des variables et fonctions

```javascript
// ✅ BON
const utilisateur = { nom: 'John', email: 'john@example.com' };
function verifierEmailUnique(email) { ... }
const DB_CONNECTION_TIMEOUT = 5000;

// ❌ MAUVAIS
const u = { n: 'John', e: 'john@example.com' };
function ve(email) { ... }
const TIMEOUT = 5000;
```

### 3. Gestion des erreurs

```javascript
// ✅ BON
try {
  const [rows] = await pool.query(query, params);
  res.json(rows);
} catch (err) {
  console.error('Erreur DB:', err.message);
  res.status(500).json({ message: 'Erreur serveur' });
}

// ❌ MAUVAIS
const [rows] = await pool.query(query, params);
res.json(rows);  // Si erreur, tout crash!
```

### 4. Authentification

```javascript
// ✅ BON
const token = jwt.sign(
  { id: user.id, role: user.role },
  process.env.JWT_SECRET,
  { expiresIn: process.env.JWT_EXPIRES_IN }
);

// ❌ MAUVAIS
const token = jwt.sign(
  { id: user.id, password: user.password },  // Ne jamais inclure le password!
  'secret123',  // Ne pas hardcoder le secret!
  { expiresIn: '999y' }
);
```

### 5. Validation des données

```javascript
// ✅ BON
if (!email || !email.includes('@')) {
  return res.status(400).json({ message: 'Email invalide' });
}

if (prix <= 0 || !Number.isFinite(prix)) {
  return res.status(400).json({ message: 'Prix invalide' });
}

if (quantite < 0 || !Number.isInteger(quantite)) {
  return res.status(400).json({ message: 'Quantité invalide' });
}

// ❌ MAUVAIS
if (email) {  // Juste vérifier que c'existe pas!
  // ...
}
```

### 6. Préparation des requêtes SQL

```javascript
// ✅ BON - Paramètres liés (Protection contre SQL injection)
const [rows] = await pool.query(
  'SELECT * FROM utilisateurs WHERE email = ? AND role = ?',
  [email, role]
);

// ❌ MAUVAIS - Concaténation directe (SQL injection!)
const [rows] = await pool.query(
  `SELECT * FROM utilisateurs WHERE email = '${email}' AND role = '${role}'`
);
```

### 7. Commentaires et documentation

```javascript
// ✅ BON
/**
 * Récupère tous les produits avec filtrage optionnel
 * @param {string} search - Terme de recherche (optionnel)
 * @param {string} categorie - Catégorie à filtrer (optionnel)
 * @returns {Promise<Array>} Tableau des produits
 */
async function getProduits(search, categorie) {
  // ...
}

// ❌ MAUVAIS
// Get products
function gp(s, c) {
  // ...
}
```

### 8. Transactions pour opérations multi-étapes

```javascript
// ✅ BON
const conn = await pool.getConnection();
try {
  await conn.beginTransaction();
  
  // Vérifier le stock
  const [produits] = await conn.query('SELECT * FROM produits WHERE id = ?', [produit_id]);
  
  // Effectuer la vente
  await conn.query('INSERT INTO ventes ...');
  
  await conn.commit();
} catch (err) {
  await conn.rollback();
  throw err;
} finally {
  conn.release();
}

// ❌ MAUVAIS
// Pas de transaction - risque d'incohérence de données
await pool.query('INSERT INTO ventes ...');
```

---

## 🔐 Sécurité

### Checklist de sécurité

- [ ] **Authentification**
  - Utiliser des tokens JWT signés
  - Expiration des tokens configurée
  - Salt pour le hash des mots de passe (bcryptjs: min 10 rounds)

- [ ] **Autorisation**
  - Vérifier les rôles pour chaque endpoint
  - Pas d'accès direct aux données sensibles
  - Isoler les ressources par utilisateur

- [ ] **Données sensibles**
  - Les variables d'environnement dans `.env`
  - Pas de secrets en dur dans le code
  - `.env` jamais committé

- [ ] **SQL Injection**
  - Utiliser les paramètres liés
  - Jamais concaténer les variables SQL

- [ ] **Mot de passe**
  - Min 8 caractères
  - Hachés avec bcryptjs
  - Jamais stockés en clair

- [ ] **CORS**
  - Restreint au domaine autorisé en production
  - Credentials configurées correctement

- [ ] **Logging**
  - Pas de données sensibles dans les logs
  - Rotation des logs
  - Alertes sur les erreurs critiques

---

## 📊 Performance

### Optimisations

```javascript
// ✅ Indexer les colonnes utilisées fréquemment
CREATE INDEX idx_email ON utilisateurs(email);
CREATE INDEX idx_date_vente ON ventes(date_vente);

// ✅ Limiter les résultats pour les listes
const ITEMS_PER_PAGE = 20;

// ✅ Utiliser le connection pooling
const pool = mysql.createPool({
  connectionLimit: 10,
  enableKeepAlive: true
});

// ✅ Cacher les résultats statiques
const cache = {};
function getCategoriesCached() {
  if (cache.categories) return cache.categories;
  // Récupérer et mettre en cache
}
```

### Points de vigilance

- ❌ N'pas de `SELECT *` pour les petites requêtes
- ❌ N'pas de boucles sur des requêtes en base
- ❌ N'pas faire de requêtes inutiles
- ❌ N'oublier les index

---

## 🧪 Testing

### Structure d'un test

```javascript
// ✅ BON
describe('Authentification', () => {
  it('devrait retourner un token valide au login', async () => {
    const response = await request(app)
      .post('/auth/login')
      .send({ email: 'admin@test.com', mot_de_passe: 'Pass123!' });
    
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('token');
  });

  it('devrait rejeter avec mauvais mot de passe', async () => {
    const response = await request(app)
      .post('/auth/login')
      .send({ email: 'admin@test.com', mot_de_passe: 'WrongPass' });
    
    expect(response.status).toBe(401);
  });
});
```

---

## 📋 Version control

### Commits conventionnels

```bash
# ✅ BON
git commit -m "feat: ajouter endpoint POST /produits"
git commit -m "fix: corriger la validation du prix"
git commit -m "docs: mettre à jour API_DOCUMENTATION.md"
git commit -m "refactor: réorganiser structure des routes"

# ❌ MAUVAIS
git commit -m "update"
git commit -m "fix bug"
git commit -m "changements divers"
```

### Branches

```bash
# ✅ BON
main                  # Code de production
develop              # Branche de développement
feature/new-api      # Nouvelle fonctionnalité
bugfix/auth-issue    # Correction de bug
hotfix/critical-err  # Correction critique

# ❌ MAUVAIS
master
dev
fix
new
test
```

---

## 🚀 Déploiement

### Avant de deployer

```bash
# ✅ Checklist pré-déploiement
npm audit              # Vérifier les vulnérabilités
npm run test          # Lancer les tests
npm run lint          # Vérifier le code (si ESLint configuré)
git status            # Aucun fichier non committé
git log --oneline     # Commits bien formattés
```

### Logs en production

```javascript
// ✅ BON
console.log('[INFO] Utilisateur connecté:', utilisateur.id);
console.error('[ERROR] Erreur DB:', err.message);

// ❌ MAUVAIS
console.log('User logged in with password:', password);  // Pas de données sensibles!
console.log(err);  // Stack trace entière exposée
```

---

## 📚 Documentation

Chaque fonction publique doit avoir:

```javascript
/**
 * Courte description
 * 
 * Explication détaillée si nécessaire
 * 
 * @param {type} param1 - Description du paramètre
 * @param {type} param2 - Description du paramètre
 * @returns {type} Description du retour
 * @throws {Error} Erreurs possibles
 * 
 * @example
 * const result = maFonction('valeur1', 'valeur2');
 */
function maFonction(param1, param2) {
  // ...
}
```

---

## ✅ Résumé des meilleurs pratiques

| Aspect | À faire | À éviter |
|--------|---------|----------|
| **Errors** | try/catch | Ignorer les erreurs |
| **Auth** | JWT signés | Secrets hardcodés |
| **SQL** | Paramètres liés | Concaténation |
| **Données** | Variables ENV | Secrets en dur |
| **Mots de passe** | bcryptjs hash | Texte clair |
| **Validation** | Stricte | Permissive |
| **Logs** | Sans données sensibles | Avec mots de passe |
| **Commits** | Conventionnels | Messages vagues |
| **Code** | Lisible, commenté | Complexe, cryptique |
| **Tests** | Automatisés | Manuels |

---

Suivre ces bonnes pratiques garantit un code:
- ✅ Sécurisé
- ✅ Performant
- ✅ Maintenable
- ✅ Scalable
- ✅ Testable
