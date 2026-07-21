package com.example.gestionmagasin.api

import com.example.gestionmagasin.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/utilisateurs")
    suspend fun getUtilisateurs(@Header("Authorization") token: String): Response<List<Utilisateur>>

    @POST("auth/utilisateurs")
    suspend fun creerUtilisateur(
        @Header("Authorization") token: String,
        @Body utilisateur: Map<String, Any>
    ): Response<MessageResponse>

    @DELETE("auth/utilisateurs/{id}")
    suspend fun supprimerUtilisateur(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    // --- Gestion des Magasins ---
    @GET("magasins")
    suspend fun getMagasins(@Header("Authorization") token: String): Response<List<Magasin>>

    @POST("magasins")
    suspend fun ajouterMagasin(
        @Header("Authorization") token: String,
        @Body magasin: Map<String, String> 
    ): Response<MessageResponse>

    @PUT("magasins/{id}")
    suspend fun modifierMagasin(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body magasin: Magasin
    ): Response<MessageResponse>

    @DELETE("magasins/{id}")
    suspend fun supprimerMagasin(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    // --- Gestion des Produits ---
    @GET("produits")
    suspend fun getProduits(
        @Header("Authorization") token: String,
        @Query("magasin_id") magasinId: Int
    ): Response<List<Produit>>

    @POST("produits")
    suspend fun ajouterProduit(
        @Header("Authorization") token: String,
        @Body produit: Produit
    ): Response<MessageResponse>

    @PUT("produits/{id}")
    suspend fun modifierProduit(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body produit: Produit
    ): Response<MessageResponse>

    @DELETE("produits/{id}")
    suspend fun supprimerProduit(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    // --- Gestion des Ventes ---
    @POST("ventes")
    suspend fun enregistrerVente(
        @Header("Authorization") token: String,
        @Body vente: Map<String, Any>
    ): Response<MessageResponse>

    @GET("ventes")
    suspend fun getVentes(
        @Header("Authorization") token: String,
        @Query("magasin_id") magasinId: Int
    ): Response<List<Vente>>

    @GET("dashboard")
    suspend fun getDashboard(
        @Header("Authorization") token: String,
        @Query("magasin_id") magasinId: Int
    ): Response<Dashboard>
}
