-- Création de la base de données
CREATE DATABASE IF NOT EXISTS gestion_magasin;
USE gestion_magasin;

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  mot_de_passe VARCHAR(255) NOT NULL,
  role ENUM('admin', 'gestionnaire_stock', 'caissier') DEFAULT 'caissier',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX (email)
);

-- Table des produits
CREATE TABLE IF NOT EXISTS produits (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(150) NOT NULL,
  prix DECIMAL(10, 2) NOT NULL,
  quantite INT NOT NULL DEFAULT 0,
  categorie VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX (categorie)
);

-- Table des ventes
CREATE TABLE IF NOT EXISTS ventes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  utilisateur_id INT NOT NULL,
  produit_id INT NOT NULL,
  quantite INT NOT NULL,
  montant DECIMAL(10, 2) NOT NULL,
  date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
  FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE CASCADE,
  INDEX (date_vente),
  INDEX (utilisateur_id)
);

-- Insertion d'un utilisateur admin par défaut (mot de passe: Admin1234!)
INSERT INTO utilisateurs (nom, email, mot_de_passe, role) VALUES
('Admin', 'admin@magasin.com', '$2a$10$Z3V0RlhH7T4q.8KqK8Z8C.k8L8M8N8O8P8Q8R8S8T8U8V8W8X8Y8Z', 'admin')
ON DUPLICATE KEY UPDATE id=id;

-- Insertion de produits d'exemple
INSERT INTO produits (nom, prix, quantite, categorie) VALUES
('Ordinateur Portable', 899.99, 15, 'Électronique'),
('Souris Wireless', 29.99, 50, 'Accessoires'),
('Clavier Mécanique', 149.99, 30, 'Accessoires'),
('Écran 27 pouces', 349.99, 10, 'Électronique'),
('Chaise Gaming', 299.99, 20, 'Mobilier')
ON DUPLICATE KEY UPDATE id=id;
