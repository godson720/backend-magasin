package com.example.gestionmagasin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.example.gestionmagasin.model.Produit
import com.example.gestionmagasin.ui.components.BarcodeScannerView
import com.example.gestionmagasin.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduitsScreen(role: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var produits by remember { mutableStateOf<List<Produit>>(emptyList()) }
    var chargement by remember { mutableStateOf(true) }
    var erreur by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var produitSelectionne by remember { mutableStateOf<Produit?>(null) }
    
    var texteRecherche by remember { mutableStateOf("") }
    var categorieSelectionnee by remember { mutableStateOf("Tous") }
    
    var showScanner by remember { mutableStateOf(false) }

    val categories = remember(produits) {
        listOf("Tous") + produits.map { it.categorie }.distinct().filter { it.isNotEmpty() }
    }

    val produitsFiltrés = produits.filter {
        val matchRecherche = it.nom.contains(texteRecherche, ignoreCase = true) || 
                           (it.code_barre != null && it.code_barre.contains(texteRecherche))
        val matchCategorie = categorieSelectionnee == "Tous" || it.categorie == categorieSelectionnee
        matchRecherche && matchCategorie
    }

    fun chargerProduits() {
        chargement = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Utilisation du magasinId de la session
                val response = RetrofitClient.instance.getProduits(
                    session.getBearerToken(), 
                    session.getMagasinId()
                )
                withContext(Dispatchers.Main) {
                    chargement = false
                    if (response.isSuccessful) produits = response.body() ?: emptyList()
                    else erreur = "Erreur chargement"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { chargement = false; erreur = "Erreur serveur" }
            }
        }
    }

    LaunchedEffect(Unit) { chargerProduits() }

    if (showScanner) {
        Box(modifier = Modifier.fillMaxSize()) {
            BarcodeScannerView(onBarcodeDetected = { code ->
                texteRecherche = code
                showScanner = false
            })
            IconButton(
                onClick = { showScanner = false },
                modifier = Modifier.padding(24.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Stock & Produits") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
                )
            },
            floatingActionButton = {
                if (role == "admin" || role == "gestionnaire_stock") {
                    FloatingActionButton(onClick = { produitSelectionne = null; showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = texteRecherche,
                        onValueChange = { texteRecherche = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Rechercher ou Scanner...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { showScanner = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = categorieSelectionnee == cat,
                            onClick = { categorieSelectionnee = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        chargement -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        erreur.isNotEmpty() -> Text(erreur, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                        produitsFiltrés.isEmpty() -> Text("Aucun produit trouvé", modifier = Modifier.align(Alignment.Center))
                        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                            items(produitsFiltrés) { produit ->
                                ProduitCard(produit, role, 
                                    onModifier = { p -> produitSelectionne = p; showDialog = true },
                                    onSupprimer = { p -> 
                                        CoroutineScope(Dispatchers.IO).launch {
                                            RetrofitClient.instance.supprimerProduit(session.getBearerToken(), p.id)
                                            withContext(Dispatchers.Main) { chargerProduits() }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProduitDialog(produitSelectionne, onDismiss = { showDialog = false }, onConfirm = { p ->
            showDialog = false
            CoroutineScope(Dispatchers.IO).launch {
                // On ajoute le magasinId actuel lors de l'ajout
                val produitAvecMagasin = p.copy(magasin_id = session.getMagasinId())
                if (produitSelectionne == null) {
                    RetrofitClient.instance.ajouterProduit(session.getBearerToken(), produitAvecMagasin)
                } else {
                    RetrofitClient.instance.modifierProduit(session.getBearerToken(), p.id, produitAvecMagasin)
                }
                withContext(Dispatchers.Main) { chargerProduits() }
            }
        })
    }
}

@Composable
fun ProduitCard(produit: Produit, role: String, onModifier: (Produit) -> Unit, onSupprimer: (Produit) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(produit.nom, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Stock : ${produit.quantite}", color = if (produit.quantite <= 5) Color.Red else Color.Gray)
                Text("${produit.prix} FCFA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                if (produit.code_barre != null) {
                    Text("Code: ${produit.code_barre}", fontSize = 10.sp, color = Color.LightGray)
                }
            }
            if (role == "admin" || role == "gestionnaire_stock") {
                IconButton(onClick = { onModifier(produit) }) { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                if (role == "admin") {
                    IconButton(onClick = { onSupprimer(produit) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun ProduitDialog(produit: Produit?, onDismiss: () -> Unit, onConfirm: (Produit) -> Unit) {
    var nom by remember { mutableStateOf(produit?.nom ?: "") }
    var prix by remember { mutableStateOf(produit?.prix?.toString() ?: "") }
    var quantite by remember { mutableStateOf(produit?.quantite?.toString() ?: "") }
    var categorie by remember { mutableStateOf(produit?.categorie ?: "") }
    var codeBarre by remember { mutableStateOf(produit?.code_barre ?: "") }
    
    var showDialogScanner by remember { mutableStateOf(false) }

    if (showDialogScanner) {
        Box(modifier = Modifier.fillMaxSize()) {
            BarcodeScannerView(onBarcodeDetected = { code ->
                codeBarre = code
                showDialogScanner = false
            })
            IconButton(
                onClick = { showDialogScanner = false },
                modifier = Modifier.padding(24.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (produit == null) "Ajouter Produit" else "Modifier Produit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = prix, onValueChange = { prix = it }, label = { Text("Prix") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = quantite, onValueChange = { quantite = it }, label = { Text("Stock Initial") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = categorie, onValueChange = { categorie = it }, label = { Text("Catégorie") }, modifier = Modifier.fillMaxWidth())
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = codeBarre, 
                            onValueChange = { codeBarre = it }, 
                            label = { Text("Code Barre") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showDialogScanner = true }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner")
                        }
                    }
                }
            },
            confirmButton = { 
                Button(onClick = { 
                    onConfirm(Produit(produit?.id ?: 0, nom, prix.toDoubleOrNull() ?: 0.0, quantite.toIntOrNull() ?: 0, categorie, if(codeBarre.isEmpty()) null else codeBarre)) 
                }) { Text("Valider") } 
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
        )
    }
}
