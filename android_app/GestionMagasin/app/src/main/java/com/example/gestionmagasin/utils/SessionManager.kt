package com.example.gestionmagasin.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gestion_magasin_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) = prefs.edit().putString("token", token).apply()
    fun getToken(): String? = prefs.getString("token", null)

    fun saveRole(role: String) = prefs.edit().putString("role", role).apply()
    fun getRole(): String? = prefs.getString("role", null)

    fun saveNom(nom: String) = prefs.edit().putString("nom", nom).apply()
    fun getNom(): String? = prefs.getString("nom", null)

    fun saveMagasinId(id: Int) = prefs.edit().putInt("magasin_id", id).apply()
    fun getMagasinId(): Int = prefs.getInt("magasin_id", -1)

    fun getBearerToken(): String = "Bearer ${getToken()}"

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
