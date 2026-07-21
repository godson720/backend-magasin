const express = require('express');
const router  = express.Router();
const pool    = require('../config/db');
const { verifierToken, autoriserRoles } = require('../middleware/auth');

// GET /dashboard — admin seulement
router.get('/', verifierToken, autoriserRoles('admin'), async (req, res) => {
  try {
    const [[{ total_produits }]] = await pool.query(
      'SELECT COUNT(*) AS total_produits FROM produits'
    );

    const [[ventesJour]] = await pool.query(
      `SELECT COUNT(*) AS nb_ventes,
              COALESCE(SUM(montant), 0) AS chiffre_affaires
       FROM   ventes
       WHERE  DATE(date_vente) = CURDATE()`
    );

    const [ruptures] = await pool.query(
      'SELECT id, nom, quantite FROM produits WHERE quantite <= 5 ORDER BY quantite ASC'
    );

    const [top_produits] = await pool.query(
      `SELECT p.nom, SUM(v.quantite) AS total_vendu
       FROM   ventes v
       JOIN   produits p ON v.produit_id = p.id
       GROUP  BY p.id
       ORDER  BY total_vendu DESC
       LIMIT  5`
    );

    res.json({
      total_produits,
      ventes_jour:      ventesJour.nb_ventes,
      chiffre_affaires: ventesJour.chiffre_affaires,
      nb_ruptures:      ruptures.length,
      produits_rupture: ruptures,
      top_produits,
    });

  } catch (err) {
    res.status(500).json({ message: 'Erreur serveur', erreur: err.message });
  }
});

module.exports = router;