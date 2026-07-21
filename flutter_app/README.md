# Flutter app - Gestion Magasin

Cette application Flutter consomme l’API du backend Node.js déjà disponible.

## Prérequis
- Flutter SDK installé
- L’API backend démarrée sur l’adresse indiquée

## Configuration
Par défaut, l’application cible :
- http://192.168.175.153:3000

Si vous utilisez un émulateur Android, remplacez l’URL dans [lib/main.dart](lib/main.dart) par :
- http://10.0.2.2:3000

## Démarrage
```bash
cd flutter_app
flutter pub get
flutter run
```

## Fonctionnalités
- Écran de connexion
- Lecture des produits depuis l’API
- Lecture du tableau de bord depuis l’API
- Stockage du token localement
