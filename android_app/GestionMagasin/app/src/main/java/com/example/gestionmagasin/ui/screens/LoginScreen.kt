package com.example.gestionmagasin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionmagasin.api.RetrofitClient
import com.example.gestionmagasin.model.LoginRequest
import com.example.gestionmagasin.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var email by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf("") }
    var chargement by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Gestion Magasin", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = motDePasse, onValueChange = { motDePasse = it }, label = { Text("Mot de passe") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                if (erreur.isNotEmpty()) Text(erreur, color = Color.Red)
                Button(
                    onClick = {
                        chargement = true
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitClient.instance.login(LoginRequest(email, motDePasse))
                                withContext(Dispatchers.Main) {
                                    chargement = false
                                    if (response.isSuccessful) {
                                        val body = response.body()!!
                                        session.saveToken(body.token)
                                        session.saveRole(body.utilisateur.role)
                                        session.saveNom(body.utilisateur.nom)
                                        onLoginSuccess(body.utilisateur.role)
                                    } else erreur = "Échec de connexion"
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) { chargement = false; erreur = "Erreur serveur" }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !chargement
                ) {
                    if (chargement) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Se connecter")
                }
            }
        }
    }
}
