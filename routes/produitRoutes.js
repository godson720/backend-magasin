const express = require('express');
const router  = express.Router();
const pool    = require('../config/db');
const { verifierToken, autoriserRoles, appliquerFiltreMagasin } = require('../middleware/auth');

router.get('/', verifierToken, appliquerFiltreMagasin, async (req, res) => {
  try {
    const { search, categorie } = req.query;
    let query    = 'SELECT * FROM produits WHERE 1=1';
    const params = [];

    if (req.magasinFiltre) {
      query += ' AND magasin_id = ?';
      params.push(req.magasinFiltre);
    }
    if (search) {
      query += ' AND nom LIKE ?';
      params.push(`%${search}%`);
    }
    if (categorie) {
      query += ' AND categorie = ?';
      params.push(categorie);
    }

    query += ' ORDER BY nom ASC';
    const [rows] = await pool.query(query, params);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.get('/:id', verifierToken, async (req, res) => {
  try {
    const [rows] = await pool.query(
      'SELECT * FROM produits WHERE id = ?', [req.params.id]
    );
    if (rows.length === 0) {
      return res.status(404).json({ message: 'Produit introuvable' });
    }
    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.post('/', verifierToken, autoriserRoles('admin', 'gestionnaire_stock'), appliquerFiltreMagasin, async (req, res) => {
  const { nom, prix, quantite, categorie, magasin_id } = req.body;

  if (!nom || prix == null || quantite == null || !categorie) {
    return res.status(400).json({ message: 'Tous les champs sont requis' });
  }

  const magasinFinal = req.utilisateur.role === 'admin' ? (magasin_id || null) : req.magasinFiltre;

  try {
    const [result] = await pool.query(
      'INSERT INTO produits (nom, prix, quantite, categorie, magasin_id) VALUES (?, ?, ?, ?, ?)',
      [nom, prix, quantite, categorie, magasinFinal]
    );
    res.status(201).json({ message: 'Produit ajoute', id: result.insertId });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.put('/:id', verifierToken, autoriserRoles('admin', 'gestionnaire_stock'), async (req, res) => {
  const { nom, prix, quantite, categorie } = req.body;

  try {
    const [result] = await pool.query(
      'UPDATE produits SET nom=?, prix=?, quantite=?, categorie=? WHERE id=?',
      [nom, prix, quantite, categorie, req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Produit introuvable' });
    }
    res.json({ message: 'Produit modifie' });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

router.delete('/:id', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    const [result] = await pool.query(
      'DELETE FROM produits WHERE id = ?', [req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Produit introuvable' });
    }
    res.json({ message: 'Produit supprime' });
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

module.exports = router;