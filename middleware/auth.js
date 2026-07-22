const jwt = require('jsonwebtoken');
require('dotenv').config();

function verifierToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  if (!token) {
    return res.status(401).json({ message: 'Token manquant' });
  }
  jwt.verify(token, process.env.JWT_SECRET, (err, utilisateur) => {
    if (err) return res.status(403).json({ message: 'Token invalide ou expire' });
    req.utilisateur = utilisateur;
    next();
  });
}

function autoriserRoles(...roles) {
  return (req, res, next) => {
    if (!roles.includes(req.utilisateur.role)) {
      return res.status(403).json({ message: 'Acces refuse' });
    }
    next();
  };
}

function appliquerFiltreMagasin(req, res, next) {
  const utilisateur = req.utilisateur;

  if (utilisateur.role === 'admin') {
    req.magasinFiltre = req.query.magasin_id || null;
  } else {
    if (!utilisateur.magasin_id) {
      return res.status(403).json({ message: 'Aucun magasin assigne a ce compte' });
    }
    req.magasinFiltre = utilisateur.magasin_id;
  }
  next();
}

module.exports = { verifierToken, autoriserRoles, appliquerFiltreMagasin };