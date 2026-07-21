const express = require('express');
const router  = express.Router();
const bcrypt  = require('bcryptjs');
const jwt     = require('jsonwebtoken');
const pool    = require('../config/db');
const { verifierToken, autoriserRoles } = require('../middleware/auth');
// POST /auth/login
router.post('/login', async (req, res) => {
  const { email, mot_de_passe } = req.body;

  if (!email || !mot_de_passe) {
    return res.status(400).json({ message: 'Email et mot de passe requis' });
  }

  try {
    const [rows] = await pool.query(
      'SELECT * FROM utilisateurs WHERE email = ?', [email]
    );

    if (rows.length === 0) {
      return res.status(401).json({ message: 'Identifiants incorrects' });
    }

    const utilisateur = rows[0];
    const motDePasseValide = await bcrypt.compare(mot_de_passe, utilisateur.mot_de_passe);

    if (!motDePasseValide) {
      return res.status(401).json({ message: 'Identifiants incorrects' });
    }

    const token = jwt.sign(
      { id: utilisateur.id, role: utilisateur.role, nom: utilisateur.nom },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN }
    );

    res.json({
      token,
      utilisateur: {
        id:    utilisateur.id,
        nom:   utilisateur.nom,
        email: utilisateur.email,
        role:  utilisateur.role,
      }
    });

  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

// GET /auth/utilisateurs — admin seulement
router.get('/utilisateurs', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    const [rows] = await pool.query(
      'SELECT id, nom, email, role, created_at FROM utilisateurs'
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

// POST /auth/utilisateurs — admin seulement
router.post('/utilisateurs', verifierToken, autoriserRoles('admin'), async (req, res) => {
  const { nom, email, mot_de_passe, role } = req.body;

  if (!nom || !email || !mot_de_passe || !role) {
    return res.status(400).json({ message: 'Tous les champs sont requis' });
  }

  try {
    const hash = await bcrypt.hash(mot_de_passe, 10);
    const [result] = await pool.query(
      'INSERT INTO utilisateurs (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)',
      [nom, email, hash, role]
    );
    res.status(201).json({ message: 'Utilisateur créé', id: result.insertId });
  } catch (err) {
    if (err.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ message: 'Email déjà utilisé' });
    }
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

// DELETE /auth/utilisateurs/:id — admin seulement
router.delete('/utilisateurs/:id', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    await pool.query('DELETE FROM utilisateurs WHERE id = ?', [req.params.id]);
    res.json({ message: 'Utilisateur supprimé' });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

module.exports = router;