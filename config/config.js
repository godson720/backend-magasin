/**
 * Configuration de l'application selon l'environnement
 */

require('dotenv').config();

const config = {
  env: process.env.NODE_ENV || 'development',
  port: process.env.PORT || 3000,
  
  database: {
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 3306,
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'gestion_magasin',
    waitForConnections: true,
    connectionLimit: process.env.NODE_ENV === 'production' ? 20 : 10,
    enableKeepAlive: true,
    keepAliveInitialDelayMs: 0,
  },
  
  jwt: {
    secret: process.env.JWT_SECRET || 'secret_par_defaut_non_securise',
    expiresIn: process.env.JWT_EXPIRES_IN || '24h',
  },
  
  cors: {
    origin: process.env.CORS_ORIGIN || '*',
    credentials: true,
    optionsSuccessStatus: 200,
  },
  
  // Limites
  limits: {
    maxJsonSize: '10kb',
    maxUrlEncodedSize: '10kb',
    requestTimeoutMs: 30000,
  },
  
  // Logging
  logging: {
    enabled: true,
    level: process.env.LOG_LEVEL || (process.env.NODE_ENV === 'development' ? 'debug' : 'info'),
  },
};

// Validation en mode production
if (config.env === 'production') {
  if (config.jwt.secret === 'secret_par_defaut_non_securise') {
    throw new Error('JWT_SECRET doit être défini en production');
  }
  if (!process.env.DB_PASSWORD) {
    throw new Error('DB_PASSWORD est requis en production');
  }
}

module.exports = config;
