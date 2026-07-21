/**
 * Rôles disponibles dans l'application
 */
const ROLES = {
  ADMIN: 'admin',
  GESTIONNAIRE_STOCK: 'gestionnaire_stock',
  CAISSIER: 'caissier'
};

/**
 * Messages d'erreur standard
 */
const MESSAGES = {
  // Authentification
  EMAIL_REQUIRED: 'Email requis',
  PASSWORD_REQUIRED: 'Mot de passe requis',
  INVALID_CREDENTIALS: 'Identifiants incorrects',
  EMAIL_ALREADY_USED: 'Email déjà utilisé',
  
  // Token
  TOKEN_MISSING: 'Token manquant',
  TOKEN_INVALID: 'Token invalide ou expiré',
  
  // Accès
  ACCESS_DENIED: 'Accès refusé',
  INSUFFICIENT_ROLE: 'Rôle insuffisant',
  
  // Ressources
  NOT_FOUND: 'Ressource introuvable',
  ALREADY_EXISTS: 'La ressource existe déjà',
  
  // Validation
  INVALID_INPUT: 'Données invalides',
  REQUIRED_FIELDS: 'Tous les champs sont requis',
  
  // Stock
  INSUFFICIENT_STOCK: 'Stock insuffisant',
  QUANTITY_POSITIVE: 'La quantité doit être positive',
  
  // Serveur
  SERVER_ERROR: 'Erreur serveur'
};

/**
 * Codes de statut HTTP personnalisés
 */
const HTTP_STATUS = {
  SUCCESS: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  SERVER_ERROR: 500
};

module.exports = {
  ROLES,
  MESSAGES,
  HTTP_STATUS
};
