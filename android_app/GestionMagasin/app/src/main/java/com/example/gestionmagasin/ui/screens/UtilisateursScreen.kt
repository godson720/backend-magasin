package com.example.gestionmagasin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionmagasin.api.RetrofitClient
import com.example.gestionmagasin.model.Utilisateur
import com.example.gestionmagasin.model.Magasin
import com.example.gestionmagasin.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilisateursScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var utilisateurs by remember { mutableStateOf<List<Utilisateur>>(emptyList()) }
    var magasins by remember { mutableStateOf<List<Magasin>>(emptyList()) }
    var chargement by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    fun chargerDonnees() {
        chargement = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resUsers = RetrofitClient.instance.getUtilisateurs(session.getBearerToken())
                val resMagasins = RetrofitClient.instance.getMagasins(session.getBearerToken())
                withContext(Dispatchers.Main) {
                    chargement = false
                    if (resUsers.isSuccessful) utilisateurs = resUsers.body() ?: emptyList()
                    if (resMagasins.isSuccessful) magasins = resMagasins.body() ?: emptyList()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { chargement = false }
            }
        }
    }

    LaunchedEffect(Unit) { chargerDonnees() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion du Personnel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (chargement) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(utilisateurs) { u ->
                        UserCard(u, onDelete = {
                            CoroutineScope(Dispatchers.IO).launch {
                                RetrofitClient.instance.supprimerUtilisateur(session.getBearerToken(), u.id)
                                withContext(Dispatchers.Main) { chargerDonnees() }
                            }
                        })
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddUserDialog(
            magasins = magasins,
            onDismiss = { showDialog = false },
            onConfirm = { userData ->
                showDialog = false
                CoroutineScope(Dispatchers.IO).launch {
                    RetrofitClient.instance.creerUtilisateur(session.getBearerToken(), userData)
                    withContext(Dispatchers.Main) { chargerDonnees() }
                }
            }
        )
    }
}

@Composable
fun UserCard(u: Utilisateur, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(u.nom, fontWeight = FontWeight.Bold)
                Text(u.email, fontSize = 12.sp, color = Color.Gray)
                Text("Rôle : ${u.role}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                if (u.magasin_id != null) {
                    Text("Affectation : Magasin #${u.magasin_id}", fontSize = 11.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(magasins: List<Magasin>, onDismiss: () -> Unit, onConfirm: (Map<String, Any>) -> Unit) {
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mdp by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("caissier") }
    
    var expanded by remember { mutableStateOf(false) }
    var magasinSelectionne by remember { mutableStateOf<Magasin?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel Employé", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Identifiant)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mdp, onValueChange = { mdp = it }, label = { Text("Mot de passe") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                
                Text("Rôle :", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == "caissier", onClick = { role = "caissier" })
                    Text("Caissier")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = role == "admin", onClick = { role = "admin" })
                    Text("Admin")
                }

                if (role == "caissier") {
                    Text("Affectation au magasin :", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(magasinSelectionne?.nom ?: "Choisir un magasin")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            magasins.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.nom) },
                                    onClick = {
                                        magasinSelectionne = m
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val data = mutableMapOf<String, Any>(
                    "nom" to nom,
                    "email" to email,
                    "mot_de_passe" to mdp,
                    "role" to role
                )
                magasinSelectionne?.let { data["magasin_id"] = it.id }
                onConfirm(data)
            }) { Text("Créer l'accès") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
