-- Suppression des ventes de test (liees au caissier de test, id utilisateur 9)
DELETE FROM ventes WHERE utilisateur_id = 9;

-- Suppression des ventes liees aux produits de test (avant de supprimer les produits)
DELETE FROM ventes WHERE produit_id IN (SELECT id FROM produits WHERE nom = 'Produit Test');

-- Suppression des utilisateurs de test
DELETE FROM utilisateurs WHERE email LIKE 'test_%@magasin.com';
DELETE FROM utilisateurs WHERE email = 'caissier.test@magasin.com';

-- Suppression des produits de test
DELETE FROM produits WHERE nom = 'Produit Test';

-- Restauration du stock decremente pendant les tests
UPDATE produits SET quantite = 50 WHERE id = 2 AND nom = 'Souris Wireless';
UPDATE produits SET quantite = 10 WHERE id = 9;