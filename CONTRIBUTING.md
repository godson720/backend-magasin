# Contribution au projet - Backend Gestion Magasin

## 🤝 Comment contribuer

Merci de votre intérêt à contribuer! Voici comment faire.

## 📋 Code de conduite

- Soyez respectueux envers les autres contributeurs
- Déclarez tout conflit d'intérêt
- Signalez les bugs et failles de sécurité de manière responsable

## 🐛 Signaler un bug

1. Vérifier que le bug n'a pas déjà été reporté
2. Fournir une description claire et détaillée
3. Incluez les étapes pour reproduire le bug
4. Mentionnez votre environnement (OS, versions, etc.)

**Exemple de rapport:**
```
Title: Login échoue avec emails contenant un plus (+)
Description: Quand je me connecte avec test+1@gmail.com, je reçois une erreur 401
Steps to reproduce:
1. Créer un utilisateur avec test+1@gmail.com
2. Essayer de se connecter
Environment: Node.js 18.0.0, MySQL 5.7, Windows 10
```

## 💡 Proposer une amélioration

1. Créer une issue pour discuter l'amélioration
2. Expliquer clairement le problème et la solution proposée
3. Attendre le feedback avant de commencer le développement

## 🔧 Processus de développement

### 1. Fork et cloner

```bash
# Fork le repo sur GitHub
# Cloner votre fork
git clone https://github.com/VOTRE_USERNAME/backend-magasin.git
cd backend-magasin

# Ajouter le repo original comme remote
git remote add upstream https://github.com/USERNAME_ORIGINAL/backend-magasin.git
```

### 2. Créer une branche

```bash
# Assurez-vous d'être sur develop
git checkout develop

# Créer une nouvelle branche
git checkout -b feature/description-de-la-feature

# Exemples:
# feature/add-pagination
# bugfix/auth-token-issue
# docs/update-installation-guide
```

### 3. Développer votre feature

```bash
# Installer les dépendances si nécessaire
npm install

# Tester votre code
npm run dev

# Vérifier que les tests passent
node tests.js
```

### 4. Commits

Utiliser le format conventionnel:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: Nouvelle fonctionnalité
- `fix`: Correction de bug
- `docs`: Modification de documentation
- `style`: Formatage du code (sans changement logique)
- `refactor`: Refonte du code (sans changement fonctionnel)
- `test`: Ajout ou modification de tests
- `chore`: Mise à jour des dépendances

**Exemples:**
```
feat(auth): ajouter authentification à 2 facteurs

Permet aux utilisateurs d'activer 2FA depuis les paramètres.
- Générer des codes TOTP
- Valider les codes au login
- Stocker les secrets de manière sécurisée

Closes #123

---

fix(produits): corriger la validation du prix

Le prix était accepté comme 0 ou négatif, maintenant il doit être > 0.

Closes #456

---

docs: mettre à jour la documentation API

- Ajouter des exemples cURL
- Clarifier les paramètres requis
- Ajouter les codes d'erreur possibles
```

### 5. Avant de pusher

```bash
# Vérifier le code
npm audit

# Formatter le code si ESLint est configuré
npm run lint --fix

# Tester à nouveau
node tests.js

# Vérifier qu'il n'y a pas de fichiers sensibles
git status | grep -E "\.(env|key|pem)$"
```

### 6. Push et créer une Pull Request

```bash
# Push votre branche
git push origin feature/description-de-la-feature

# Créer une PR sur GitHub
# - Titre clair et concis
# - Description détaillée
# - Lier les issues pertinentes (#123)
# - Décrire les tests effectués
```

## 📝 Template de Pull Request

```markdown
## Description
Brève description de ce que cette PR fait.

## Type de changement
- [ ] Correction de bug
- [ ] Nouvelle fonctionnalité
- [ ] Modification de documentation
- [ ] Refonte de code

## Linked Issues
Closes #123

## Tests
- [x] Tests unitaires passent
- [x] Tests manuels effectués
- [x] Pas de regression détectée

## Checklist
- [x] Mon code suit les bonnes pratiques
- [x] J'ai documenté les changements
- [x] J'ai testé les changements
- [x] Pas de dépendances inutiles ajoutées
```

## 🎯 Directives de code

### Style de code

```javascript
// ✅ À faire
const { email, password } = req.body;
if (!email || !password) {
  return res.status(400).json({ message: 'Email et password requis' });
}

// ❌ À ne pas faire
var email = req.body.email;
var password = req.body.password;
if (!email) return res.status(400).json({message:'Email needed'});
```

### Fichiers à modifier

**À modifier:**
- Routes dans `routes/`
- Middleware dans `middleware/`
- Configuration dans `config/`
- Documentation `.md`

**À ne pas modifier directement:**
- `package.json` (sauf pour les dépendances)
- `server.js` (sauf bugfix critique)
- Les fichiers de test sont à mettre à jour si nécessaire

## 🧪 Tests

Tout nouveau code doit être testé:

```bash
# Ajouter un test dans tests.js
test('Mon test', 'GET', '/endpoint', null, 200, token);

# Lancer les tests
node tests.js
```

## 📚 Documentation

Chaque nouvelle fonctionnalité doit avoir une documentation:

1. Commentaires dans le code
2. Mise à jour de `API_DOCUMENTATION.md` si c'est un nouvel endpoint
3. Mise à jour de `README.md` si c'est une grosse feature
4. Mise à jour de `BEST_PRACTICES.md` si ça affecte les pratiques

## 🔒 Sécurité

**Important:** Si vous trouvez une faille de sécurité:

1. **Ne pas** la poster sur GitHub issues
2. **Contactez** le mainteneur en privé
3. Donnez-lui le temps de corriger avant de divulguer

Format du rapport:
```
Email: security@example.com
Subject: [SECURITY] Description brève

- Description détaillée
- Étapes pour reproduire
- Impact potentiel
- Suggestion de correction (optionnel)
```

## 🚀 Processus de review

1. Un mainteneur va vérifier votre PR
2. Des changements peuvent être demandés
3. Une fois approuvée, votre PR sera mergée
4. Votre contribution apparaîtra dans les releases notes

## ✅ Checklist finale

Avant de créer votre PR, vérifiez:

- [ ] Branche créée depuis `develop`
- [ ] Commits bien formatés
- [ ] Code suit les conventions
- [ ] Tests passent (`node tests.js`)
- [ ] Pas de `console.log` de débogage
- [ ] Pas de secrets/credentials
- [ ] Documentation mise à jour
- [ ] Pas de warnings npm
- [ ] Changements liés à une issue

## 🎓 Ressources

- [Express.js Guide](https://expressjs.com/guides)
- [Git workflow](https://git-scm.com/book)
- [Conventional Commits](https://www.conventionalcommits.org)
- [Code Review Best Practices](https://google.github.io/eng-practices/review)

## 📈 Niveaux de contribution

### Débutant
- Corriger les typos dans la documentation
- Ajouter des cas de test
- Améliorer les commentaires du code

### Intermédiaire
- Corriger des bugs simples
- Ajouter de petites features
- Améliorer la performance

### Avancé
- Grandes refactorisations
- Nouvelles architectures
- Décisions d'design majeurs

## 🙏 Remerciements

Merci de contribuer! Chaque contribution, peu importe sa taille, est appréciée.

---

**Questions?** Ouvrez une issue ou contactez le mainteneur.
