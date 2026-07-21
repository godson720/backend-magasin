/**
 * Tests basiques de l'API - À exécuter après le déploiement
 * Utiliser avec npm test (après configuration de Jest)
 */

const http = require('http');
const BASE_URL = 'http://localhost:3000';

// Couleurs pour le terminal
const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m'
};

let testsPassed = 0;
let testsFailed = 0;
let token = null;

/**
 * Effectuer une requête HTTP
 */
function makeRequest(method, path, body = null, authToken = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, BASE_URL);
    const options = {
      hostname: url.hostname,
      port: url.port || 3000,
      path: url.pathname + url.search,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    if (authToken) {
      options.headers['Authorization'] = `Bearer ${authToken}`;
    }

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          resolve({
            status: res.statusCode,
            data: data ? JSON.parse(data) : null,
            headers: res.headers
          });
        } catch (e) {
          resolve({
            status: res.statusCode,
            data: data,
            headers: res.headers
          });
        }
      });
    });

    req.on('error', reject);

    if (body) {
      req.write(JSON.stringify(body));
    }
    req.end();
  });
}

/**
 * Vérifier un test
 */
async function test(name, method, path, body = null, expectedStatus = 200, token = null) {
  try {
    console.log(`\n${colors.blue}Testing: ${name}${colors.reset}`);
    console.log(`${colors.yellow}${method} ${path}${colors.reset}`);

    const response = await makeRequest(method, path, body, token);

    if (response.status === expectedStatus) {
      console.log(`${colors.green}✓ PASS${colors.reset} - Status: ${response.status}`);
      testsPassed++;
      return response.data;
    } else {
      console.log(`${colors.red}✗ FAIL${colors.reset} - Expected ${expectedStatus}, got ${response.status}`);
      console.log(`Response:`, response.data);
      testsFailed++;
      return null;
    }
  } catch (err) {
    console.log(`${colors.red}✗ ERROR${colors.reset} - ${err.message}`);
    testsFailed++;
    return null;
  }
}

/**
 * Exécuter tous les tests
 */
async function runTests() {
  console.log(`\n${colors.blue}╔════════════════════════════════════════╗${colors.reset}`);
  console.log(`${colors.blue}║   Backend Gestion Magasin - Tests API  ║${colors.reset}`);
  console.log(`${colors.blue}╚════════════════════════════════════════╝${colors.reset}\n`);

  // 1. TEST LOGIN
  console.log(`\n${colors.blue}═══ AUTHENTIFICATION ═══${colors.reset}`);
  const loginResponse = await test(
    'Login avec admin',
    'POST',
    '/auth/login',
    {
      email: 'admin@magasin.com',
      mot_de_passe: 'Admin1234!'
    },
    200
  );

  if (loginResponse && loginResponse.token) {
    token = loginResponse.token;
    console.log(`${colors.green}Token reçu!${colors.reset}`);
  } else {
    console.log(`${colors.red}Impossible de récupérer le token!${colors.reset}`);
    return;
  }

  // 2. TEST PRODUITS
  console.log(`\n${colors.blue}═══ PRODUITS ═══${colors.reset}`);

  await test(
    'Lister tous les produits',
    'GET',
    '/produits',
    null,
    200,
    token
  );

  await test(
    'Obtenir un produit (ID: 1)',
    'GET',
    '/produits/1',
    null,
    200,
    token
  );

  await test(
    'Ajouter un produit',
    'POST',
    '/produits',
    {
      nom: 'Produit Test',
      prix: 49.99,
      quantite: 10,
      categorie: 'Test'
    },
    201,
    token
  );

  await test(
    'Chercher des produits',
    'GET',
    '/produits?search=Ordinateur',
    null,
    200,
    token
  );

  // 3. TEST UTILISATEURS
  console.log(`\n${colors.blue}═══ UTILISATEURS ═══${colors.reset}`);

  await test(
    'Lister les utilisateurs',
    'GET',
    '/auth/utilisateurs',
    null,
    200,
    token
  );

  await test(
    'Créer un utilisateur',
    'POST',
    '/auth/utilisateurs',
    {
      nom: 'Test User',
      email: `test_${Date.now()}@magasin.com`,
      mot_de_passe: 'TestPassword123!',
      role: 'caissier'
    },
    201,
    token
  );

  // 4. TEST VENTES
  console.log(`\n${colors.blue}═══ VENTES ═══${colors.reset}`);

  await test(
    'Enregistrer une vente',
    'POST',
    '/ventes',
    {
      produit_id: 1,
      quantite: 1
    },
    201,
    token
  );

  await test(
    'Lister les ventes',
    'GET',
    '/ventes',
    null,
    200,
    token
  );

  // 5. TEST DASHBOARD
  console.log(`\n${colors.blue}═══ DASHBOARD ═══${colors.reset}`);

  await test(
    'Obtenir les statistiques',
    'GET',
    '/dashboard',
    null,
    200,
    token
  );

  // 6. TESTS D'ERREUR
  console.log(`\n${colors.blue}═══ VÉRIFICATION DES ERREURS ═══${colors.reset}`);

  await test(
    'Accès sans token (devrait échouer)',
    'GET',
    '/produits',
    null,
    401,
    null
  );

  await test(
    'Produit inexistant (devrait échouer)',
    'GET',
    '/produits/99999',
    null,
    404,
    token
  );

  // 7. RÉSUMÉ
  console.log(`\n${colors.blue}╔════════════════════════════════════════╗${colors.reset}`);
  console.log(`${colors.blue}║             RÉSUMÉ DES TESTS           ║${colors.reset}`);
  console.log(`${colors.blue}╚════════════════════════════════════════╝${colors.reset}`);
  console.log(`\n${colors.green}✓ Tests réussis: ${testsPassed}${colors.reset}`);
  console.log(`${colors.red}✗ Tests échoués: ${testsFailed}${colors.reset}`);

  const total = testsPassed + testsFailed;
  const percentage = ((testsPassed / total) * 100).toFixed(2);
  console.log(`\nTaux de réussite: ${colors.blue}${percentage}%${colors.reset}`);

  if (testsFailed === 0) {
    console.log(`\n${colors.green}🎉 Tous les tests sont passés!${colors.reset}\n`);
    process.exit(0);
  } else {
    console.log(`\n${colors.red}⚠️  Quelques tests ont échoué!${colors.reset}\n`);
    process.exit(1);
  }
}

// Attendre que le serveur soit prêt
setTimeout(() => {
  runTests().catch(err => {
    console.error(`${colors.red}Erreur: ${err.message}${colors.reset}`);
    process.exit(1);
  });
}, 1000);

module.exports = { makeRequest, test, runTests };
