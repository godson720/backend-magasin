const express = require('express');
const router  = express.Router();
const pool    = require('../config/db');
const { verifierToken, autoriserRoles } = require('../middleware/auth');

// POST /ventes — admin et caissier
router.post('/', verifierToken, autoriserRoles('admin', 'caissier'), async (req, res) => {
  const { produit_id, quantite } = req.body;

  if (!produit_id || !quantite || quantite <= 0) {
    return res.status(400).json({ message: 'produit_id et quantite (> 0) requis' });
  }

  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();

    const [produits] = await conn.query(
      'SELECT * FROM produits WHERE id = ? FOR UPDATE', [produit_id]
    );

    if (produits.length === 0) {
      await conn.rollback();
      return res.status(404).json({ message: 'Produit introuvable' });
    }

    const produit = produits[0];

    if (produit.quantite < quantite) {
      await conn.rollback();
      return res.status(400).json({
        message:           'Stock insuffisant',
        stock_disponible:  produit.quantite,
      });
    }

    const montant = produit.prix * quantite;

    const [result] = await conn.query(
      'INSERT INTO ventes (utilisateur_id, produit_id, quantite, montant) VALUES (?, ?, ?, ?)',
      [req.utilisateur.id, produit_id, quantite, montant]
    );

    await conn.commit();

    res.status(201).json({
      message: 'Vente enregistrée',
      id:      result.insertId,
      montant,
      produit: produit.nom,
    });

  } catch (err) {
    await conn.rollback();
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  } finally {
    conn.release();
  }
});

// GET /ventes — admin seulement
router.get('/', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    const { date } = req.query;
    let query = `
      SELECT v.id, v.quantite, v.montant, v.date_vente,
             p.nom AS produit, u.nom AS caissier
      FROM   ventes v
      JOIN   produits p     ON v.produit_id     = p.id
      JOIN   utilisateurs u ON v.utilisateur_id = u.id
    `;
    const params = [];

    if (date) {
      query += ' WHERE DATE(v.date_vente) = ?';
      params.push(date);
    }

    query += ' ORDER BY v.date_vente DESC';
    const [rows] = await pool.query(query, params);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

module.exports = router;