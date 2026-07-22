const express = require('express');
const router  = express.Router();
const pool    = require('../config/db');
const { verifierToken, autoriserRoles } = require('../middleware/auth');

router.get('/', verifierToken, async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM magasins ORDER BY nom ASC');
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.post('/', verifierToken, autoriserRoles('admin'), async (req, res) => {
  const { nom, adresse } = req.body;
  if (!nom) {
    return res.status(400).json({ message: 'Le nom du magasin est requis' });
  }
  try {
    const [result] = await pool.query(
      'INSERT INTO magasins (nom, adresse) VALUES (?, ?)',
      [nom, adresse || null]
    );
    res.status(201).json({ message: 'Magasin cree', id: result.insertId });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.put('/:id', verifierToken, autoriserRoles('admin'), async (req, res) => {
  const { nom, adresse } = req.body;
  try {
    const [result] = await pool.query(
      'UPDATE magasins SET nom = ?, adresse = ? WHERE id = ?',
      [nom, adresse, req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Magasin introuvable' });
    }
    res.json({ message: 'Magasin modifie' });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.delete('/:id', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    const [result] = await pool.query('DELETE FROM magasins WHERE id = ?', [req.params.id]);
    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Magasin introuvable' });
    }
    res.json({ message: 'Magasin supprime' });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

module.exports = router;