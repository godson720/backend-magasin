const pool = require('./config/db');

async function run() {
  try {
    // Vérifier si un produit avec id=1 existe
    const [rows] = await pool.query('SELECT id FROM produits WHERE id = 1');
    if (rows.length === 0) {
      await pool.query(
        `INSERT INTO produits (id, nom, prix, quantite, categorie)
         VALUES (1, 'Ordinateur Portable', 899.99, 15, 'Électronique')`
      );
      console.log('Produit id=1 inséré');
    } else {
      console.log('Produit id=1 déjà présent');
    }

    // Insérer produits supplémentaires si table vide
    const [countRows] = await pool.query('SELECT COUNT(*) AS cnt FROM produits');
    if (countRows[0].cnt < 5) {
      await pool.query(
        `INSERT INTO produits (nom, prix, quantite, categorie) VALUES
        ('Souris Wireless', 29.99, 50, 'Accessoires'),
        ('Clavier Mécanique', 149.99, 30, 'Accessoires'),
        ('Écran 27 pouces', 349.99, 10, 'Électronique'),
        ('Chaise Gaming', 299.99, 20, 'Mobilier')`
      );
      console.log('Produits d\'exemple insérés');
    } else {
      console.log('Suffisamment de produits présents');
    }

    process.exit(0);
  } catch (err) {
    console.error('Erreur seedProducts:', err.message);
    process.exit(1);
  }
}

run();
