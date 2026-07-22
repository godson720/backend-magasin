const express = require('express');
const router  = express.Router();
const pool    = require('../config/db');
const { verifierToken, autoriserRoles, appliquerFiltreMagasin } = require('../middleware/auth');

router.post('/', verifierToken, autoriserRoles('admin', 'caissier', 'comptable'), appliquerFiltreMagasin, async (req, res) => {
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

    if (req.utilisateur.role !== 'admin' && produit.magasin_id !== req.utilisateur.magasin_id) {
      await conn.rollback();
      return res.status(403).json({ message: 'Ce produit n appartient pas a votre magasin' });
    }

    if (produit.quantite < quantite) {
      await conn.rollback();
      return res.status(400).json({
        message:           'Stock insuffisant',
        stock_disponible:  produit.quantite,
      });
    }

    const montant = produit.prix * quantite;
    const magasinVente = req.utilisateur.role === 'admin' ? produit.magasin_id : req.utilisateur.magasin_id;

    const [result] = await conn.query(
      'INSERT INTO ventes (utilisateur_id, produit_id, quantite, montant, magasin_id) VALUES (?, ?, ?, ?, ?)',
      [req.utilisateur.id, produit_id, quantite, montant, magasinVente]
    );

    await conn.commit();

    res.status(201).json({
      message: 'Vente enregistree',
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

router.get('/', verifierToken, autoriserRoles('admin', 'caissier', 'comptable'), appliquerFiltreMagasin, async (req, res) => {
  try {
    const { date } = req.query;
    let query = `
      SELECT v.id, v.quantite, v.montant, v.date_vente,
             p.nom AS produit, u.nom AS caissier
      FROM   ventes v
      JOIN   produits p     ON v.produit_id     = p.id
      JOIN   utilisateurs u ON v.utilisateur_id = u.id
      WHERE  1=1
    `;
    const params = [];

    if (req.magasinFiltre) {
      query += ' AND v.magasin_id = ?';
      params.push(req.magasinFiltre);
    }
    if (date) {
      query += ' AND DATE(v.date_vente) = ?';
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