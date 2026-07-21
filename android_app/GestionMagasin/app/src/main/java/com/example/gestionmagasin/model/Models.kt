package com.example.gestionmagasin.model

data class Magasin(
    val id: Int = 0,
    val nom: String,
    val adresse: String
)

data class LoginRequest(
    val email: String,
    val mot_de_passe: String
)

data class LoginResponse(
    val token: String,
    val utilisateur: Utilisateur,
    val magasins: List<Magasin> = emptyList()
)

data class Utilisateur(
    val id: Int,
    val nom: String,
    val email: String,
    val role: String,
    val magasin_id: Int? = null
)

data class Produit(
    val id: Int = 0,
    val nom: String,
    val prix: Double,
    val quantite: Int,
    val categorie: String,
    val code_barre: String? = null,
    val magasin_id: Int = 0
)

data class Vente(
    val id: Int = 0,
    val produit_id: Int,
    val quantite: Int,
    val montant: Double = 0.0,
    val date_vente: String = "",
    val magasin_id: Int = 0
)

data class Dashboard(
    val total_produits: Int,
    val ventes_jour: Int,
    val chiffre_affaires: Double,
    val nb_ruptures: Int,
    val produits_rupture: List<Produit>,
    val top_produits: List<TopProduit>
)

data class TopProduit(
    val nom: String,
    val total_vendu: Int
)

data class MessageResponse(
    val message: String,
    val id: Int? = null
)
