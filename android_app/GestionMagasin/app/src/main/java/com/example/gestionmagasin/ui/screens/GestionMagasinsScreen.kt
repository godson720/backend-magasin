package com.example.gestionmagasin.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionmagasin.api.RetrofitClient
import com.example.gestionmagasin.model.Magasin
import com.example.gestionmagasin.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionMagasinsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var magasins by remember { mutableStateOf<List<Magasin>>(emptyList()) }
    var chargement by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var magasinSelectionne by remember { mutableStateOf<Magasin?>(null) }

    fun chargerMagasins() {
        chargement = true
        scope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getMagasins(session.getBearerToken())
                withContext(Dispatchers.Main) {
                    chargement = false
                    if (response.isSuccessful) {
                        magasins = response.body() ?: emptyList()
                    } else {
                        snackbarHostState.showSnackbar("Erreur de chargement : ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chargement = false
                    snackbarHostState.showSnackbar("Erreur réseau")
                }
            }
        }
    }

    LaunchedEffect(Unit) { chargerMagasins() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Paramètres des Magasins") },
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
            FloatingActionButton(onClick = {
                magasinSelectionne = null
                showDialog = true
            }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (chargement) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (magasins.isEmpty()) {
                Text("Aucun magasin trouvé. Ajoutez-en un !", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(magasins) { magasin ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(magasin.nom, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(magasin.adresse, fontSize = 14.sp, color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = {
                                        magasinSelectionne = magasin
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            val res = RetrofitClient.instance.supprimerMagasin(session.getBearerToken(), magasin.id)
                                            withContext(Dispatchers.Main) {
                                                if (res.isSuccessful) chargerMagasins()
                                                else snackbarHostState.showSnackbar("Erreur suppression")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        MagasinDialog(
            magasin = magasinSelectionne,
            onDismiss = { showDialog = false },
            onConfirm = { m ->
                showDialog = false
                scope.launch(Dispatchers.IO) {
                    try {
                        val response = if (magasinSelectionne == null) {
                            // Envoi des infos sans l'ID pour la création
                            RetrofitClient.instance.ajouterMagasin(
                                session.getBearerToken(),
                                mapOf("nom" to m.nom, "adresse" to m.adresse)
                            )
                        } else {
                            RetrofitClient.instance.modifierMagasin(session.getBearerToken(), m.id, m)
                        }
                        
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                chargerMagasins()
                                snackbarHostState.showSnackbar("Enregistré avec succès")
                            } else {
                                snackbarHostState.showSnackbar("Erreur : ${response.code()}")
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar("Erreur de connexion")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MagasinDialog(magasin: Magasin?, onDismiss: () -> Unit, onConfirm: (Magasin) -> Unit) {
    var nom by remember { mutableStateOf(magasin?.nom ?: "") }
    var adresse by remember { mutableStateOf(magasin?.adresse ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (magasin == null) "Nouveau Magasin" else "Modifier Magasin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom du magasin") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = adresse, onValueChange = { adresse = it }, label = { Text("Adresse / Ville") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nom.isNotBlank()) {
                    onConfirm(Magasin(id = magasin?.id ?: 0, nom = nom, adresse = adresse))
                }
            }) { Text("Confirmer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
