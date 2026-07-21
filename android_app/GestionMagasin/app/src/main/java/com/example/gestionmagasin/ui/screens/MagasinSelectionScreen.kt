package com.example.gestionmagasin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
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
fun MagasinSelectionScreen(onMagasinSelected: (Int, String) -> Unit, onManageMagasins: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var magasins by remember { mutableStateOf<List<Magasin>>(emptyList()) }
    var chargement by remember { mutableStateOf(true) }

    fun chargerMagasins() {
        chargement = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMagasins(session.getBearerToken())
                withContext(Dispatchers.Main) {
                    chargement = false
                    if (response.isSuccessful) {
                        magasins = response.body() ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { chargement = false }
            }
        }
    }

    LaunchedEffect(Unit) { chargerMagasins() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sélection du Magasin") },
                actions = {
                    IconButton(onClick = onManageMagasins) {
                        Icon(Icons.Default.Settings, contentDescription = "Gérer", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Tableau de Bord Administrateur",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (chargement) {
                CircularProgressIndicator()
            } else if (magasins.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aucun magasin configuré.", modifier = Modifier.padding(bottom = 16.dp))
                    Button(onClick = onManageMagasins) {
                        Text("Ajouter mon premier magasin")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(magasins) { magasin ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onMagasinSelected(magasin.id, magasin.nom) },
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.width(20.dp))
                                Column {
                                    Text(magasin.nom, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(magasin.adresse, fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
