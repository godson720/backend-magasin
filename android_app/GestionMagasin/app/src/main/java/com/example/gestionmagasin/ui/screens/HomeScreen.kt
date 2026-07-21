package com.example.gestionmagasin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.example.gestionmagasin.utils.SessionManager

@Composable
fun HomeScreen(role: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val nom = session.getNom() ?: "Utilisateur"
    val magasinId = session.getMagasinId()

    // Liste des noms de magasins pour l'affichage (à lier avec l'API plus tard)
    val magasinNom = when(magasinId) {
        1 -> "Magasin Central"
        2 -> "Boutique Sud"
        3 -> "Point de Vente Est"
        else -> "Magasin sélectionné"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // En-tête avec info magasin
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = magasinNom, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "Rôle : $role", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Text(
            text = "Bienvenue, $nom",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Boutons de navigation
        Button(
            onClick = { onNavigate("produits") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Gestion du Stock") }

        if (role == "admin" || role == "caissier") {
            Button(
                onClick = { onNavigate("ventes") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Effectuer une Vente") }
        }

        if (role == "admin") {
            Button(
                onClick = { onNavigate("dashboard") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Tableau de bord") }

            Button(
                onClick = { onNavigate("rapports") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Rapports d'Activité") }

            Button(
                onClick = { onNavigate("utilisateurs") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Gestion du Personnel") }
            
            Button(
                onClick = { onNavigate("gestion_magasins") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("Paramètres Magasins") }
            
            // Nouveau bouton pour changer de magasin
            OutlinedButton(
                onClick = { onNavigate("select_magasin") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) { 
                Text("Changer de Magasin") 
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) { 
            Text("Fermer la session", color = Color.Red) 
        }
    }
}
