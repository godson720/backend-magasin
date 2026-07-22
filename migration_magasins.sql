CREATE TABLE IF NOT EXISTS magasins (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(255) NOT NULL,
  adresse VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE produits ADD COLUMN magasin_id INT NULL;
ALTER TABLE ventes ADD COLUMN magasin_id INT NULL;
ALTER TABLE utilisateurs ADD COLUMN magasin_id INT NULL;

ALTER TABLE utilisateurs MODIFY COLUMN role
  ENUM('admin', 'gestionnaire_stock', 'caissier', 'comptable') DEFAULT 'caissier';

ALTER TABLE produits ADD CONSTRAINT fk_produits_magasin
  FOREIGN KEY (magasin_id) REFERENCES magasins(id) ON DELETE SET NULL;
ALTER TABLE ventes ADD CONSTRAINT fk_ventes_magasin
  FOREIGN KEY (magasin_id) REFERENCES magasins(id) ON DELETE SET NULL;
ALTER TABLE utilisateurs ADD CONSTRAINT fk_utilisateurs_magasin
  FOREIGN KEY (magasin_id) REFERENCES magasins(id) ON DELETE SET NULL;

CREATE INDEX idx_produits_magasin ON produits(magasin_id);
CREATE INDEX idx_ventes_magasin ON ventes(magasin_id);
CREATE INDEX idx_utilisateurs_magasin ON utilisateurs(magasin_id);

INSERT INTO magasins (nom, adresse) VALUES ('Magasin Principal', 'A completer');
UPDATE produits SET magasin_id = 1 WHERE magasin_id IS NULL;
UPDATE ventes SET magasin_id = 1 WHERE magasin_id IS NULL;