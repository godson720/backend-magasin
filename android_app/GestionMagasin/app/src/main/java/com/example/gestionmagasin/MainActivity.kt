package com.example.gestionmagasin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gestionmagasin.ui.screens.*
import com.example.gestionmagasin.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen(onFinished = { showSplash = false })
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(300.dp)
                            .align(Alignment.Center)
                            .alpha(0.05f)
                    )
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    
    // Déterminer l'écran initial en fonction de l'état de connexion et du magasin choisi
    val initialScreen = when {
        !session.isLoggedIn() -> "login"
        session.getRole() == "admin" && session.getMagasinId() == -1 -> "select_magasin"
        else -> "home"
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }
    var role by remember { mutableStateOf(session.getRole() ?: "") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { newRole: String ->
                role = newRole
                if (newRole == "admin") {
                    currentScreen = "select_magasin"
                } else {
                    currentScreen = "home"
                }
            }
        )
        "select_magasin" -> MagasinSelectionScreen(
            onMagasinSelected = { id: Int, _: String ->
                session.saveMagasinId(id)
                currentScreen = "home"
            },
            onManageMagasins = {
                currentScreen = "gestion_magasins"
            }
        )
        "home" -> HomeScreen(
            role = role,
            onNavigate = { dest: String ->
                if (dest == "select_magasin") {
                    session.saveMagasinId(-1) // Permet de rechoisir le magasin
                }
                currentScreen = dest
            },
            onLogout = {
                session.logout()
                currentScreen = "login"
            }
        )
        "produits" -> ProduitsScreen(
            role = role,
            onBack = { currentScreen = "home" }
        )
        "ventes" -> VentesScreen(
            onBack = { currentScreen = "home" }
        )
        "dashboard" -> DashboardScreen(
            onBack = { currentScreen = "home" }
        )
        "utilisateurs" -> UtilisateursScreen(
            onBack = { currentScreen = "home" }
        )
        "rapports" -> RapportsScreen(
            onBack = { currentScreen = "home" }
        )
        "gestion_magasins" -> GestionMagasinsScreen(
            onBack = { 
                // Retourne au sélecteur si aucun magasin n'est choisi, sinon à l'accueil
                currentScreen = if (session.getMagasinId() == -1) "select_magasin" else "home"
            }
        )
    }
}
