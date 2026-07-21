/**
 * Validation des données d'entrée
 */

/**
 * Valide un email
 * @param {string} email - L'email à valider
 * @returns {boolean}
 */
function validerEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
}

/**
 * Valide un mot de passe (minimum 8 caractères)
 * @param {string} motDePasse - Le mot de passe à valider
 * @returns {boolean}
 */
function validerMotDePasse(motDePasse) {
  return motDePasse && motDePasse.length >= 8;
}

/**
 * Valide un prix (doit être un nombre positif)
 * @param {number} prix - Le prix à valider
 * @returns {boolean}
 */
function validerPrix(prix) {
  return !isNaN(prix) && prix > 0;
}

/**
 * Valide une quantité (doit être un nombre entier positif)
 * @param {number} quantite - La quantité à valider
 * @returns {boolean}
 */
function validerQuantite(quantite) {
  return Number.isInteger(quantite) && quantite > 0;
}

/**
 * Valide un rôle
 * @param {string} role - Le rôle à valider
 * @returns {boolean}
 */
function validerRole(role) {
  const rolesValides = ['admin', 'gestionnaire_stock', 'caissier'];
  return rolesValides.includes(role);
}

module.exports = {
  validerEmail,
  validerMotDePasse,
  validerPrix,
  validerQuantite,
  validerRole
};
