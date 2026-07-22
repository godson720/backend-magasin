const express = require('express');
const cors    = require('cors');
require('dotenv').config();

const authRoutes      = require('./routes/authRoutes');
const produitRoutes   = require('./routes/produitRoutes');
const venteRoutes     = require('./routes/venteRoutes');
const dashboardRoutes = require('./routes/dashboardRoutes');
const magasinRoutes   = require('./routes/magasinRoutes');

const app  = express();
const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '0.0.0.0';

app.use(cors());
app.use(express.json());

app.use('/auth',      authRoutes);
app.use('/produits',  produitRoutes);
app.use('/ventes',    venteRoutes);
app.use('/dashboard', dashboardRoutes);
app.use('/magasins',  magasinRoutes);

app.get('/', (req, res) => {
  res.json({ message: 'API Gestion Magasin en ligne' });
});

app.listen(PORT, HOST, () => {
  console.log(`Serveur demarre sur http://${HOST}:${PORT}`);
});