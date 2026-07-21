/**
 * Classe personnalisée pour les erreurs API
 */
class ApiError extends Error {
  constructor(message, statusCode = 500) {
    super(message);
    this.statusCode = statusCode;
    this.name = this.constructor.name;
  }
}

/**
 * Middleware de gestion des erreurs
 */
function gestionnaireErreurs(err, req, res, next) {
  console.error(`[Erreur] ${err.name}:`, err.message);

  if (err instanceof ApiError) {
    return res.status(err.statusCode).json({
      succes: false,
      message: err.message,
      erreur: process.env.NODE_ENV === 'development' ? err.stack : undefined
    });
  }

  // Erreur MySQL
  if (err.code && err.code.startsWith('ER_')) {
    if (err.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({
        succes: false,
        message: 'Cette ressource existe déjà'
      });
    }
    return res.status(400).json({
      succes: false,
      message: 'Erreur de base de données',
      erreur: process.env.NODE_ENV === 'development' ? err.message : undefined
    });
  }

  // Erreur générique
  res.status(500).json({
    succes: false,
    message: 'Erreur serveur interne',
    erreur: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
}

module.exports = {
  ApiError,
  gestionnaireErreurs
};
