const pool = require('./config/db');
const bcrypt = require('bcryptjs');
require('dotenv').config();

const ADMIN_EMAIL = process.env.ADMIN_EMAIL || 'admin@magasin.com';
const ADMIN_PASS  = process.env.ADMIN_PASSWORD || 'Admin1234!';
const ADMIN_NAME  = process.env.ADMIN_NAME || 'Admin';

async function run() {
  try {
    const hash = await bcrypt.hash(ADMIN_PASS, 10);
    const [result] = await pool.query(
      `INSERT INTO utilisateurs (nom, email, mot_de_passe, role)
       VALUES (?, ?, ?, ?)
       ON DUPLICATE KEY UPDATE nom = VALUES(nom), mot_de_passe = VALUES(mot_de_passe), role = VALUES(role)`,
      [ADMIN_NAME, ADMIN_EMAIL, hash, 'admin']
    );

    console.log('Admin upserted:', ADMIN_EMAIL);
    process.exit(0);
  } catch (err) {
    console.error('Erreur seedAdmin:', err.message);
    process.exit(1);
  }
}

run();
