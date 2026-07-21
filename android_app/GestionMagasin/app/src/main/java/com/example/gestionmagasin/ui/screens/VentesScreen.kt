package com.example.gestionmagasin.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var produits by remember { mutableStateOf<List<Produit>>(emptyList()) }
    var produitSelectionne by remember { mutableStateOf<Produit?>(null) }
    var quantite by remember { mutableStateOf("") }
    var reduction by remember { mutableStateOf("0") }
    var texteRecherche by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Green) }
    var chargement by remember { mutableStateOf(false) }

    // État pour le scanner
    var showScanner by remember { mutableStateOf(false) }

    val produitsFiltrés = produits.filter {
        it.nom.contains(texteRecherche, ignoreCase = true) || 
        it.categorie.contains(texteRecherche, ignoreCase = true) ||
        (it.code_barre != null && it.code_barre.contains(texteRecherche))
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            // Filtrer par magasinId de la session
            val response = RetrofitClient.instance.getProduits(
                session.getBearerToken(),
                session.getMagasinId()
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) produits = response.body() ?: emptyList()
            }
        }
    }

    if (showScanner) {
        Box(modifier = Modifier.fillMaxSize()) {
            BarcodeScannerView(onBarcodeDetected = { code ->
                val produitTrouve = produits.find { it.code_barre == code }
                if (produitTrouve != null) {
                    produitSelectionne = produitTrouve
                    showScanner = false
                    message = "Produit scanné : ${produitTrouve.nom}"
                    messageColor = Color.Green
                } else {
                    texteRecherche = code // On met le code dans la recherche si non trouvé
                    showScanner = false
                    message = "Code barre inconnu : $code"
                    messageColor = Color.Red
                }
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
                    title = { Text("Nouvelle Vente") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = texteRecherche,
                        onValueChange = { texteRecherche = it },
                        label = { Text("Chercher ou Scanner...") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { showScanner = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(produitsFiltrés) { produit ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (produitSelectionne?.id == produit.id)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            onClick = { 
                                produitSelectionne = produit
                                reduction = "0" 
                            }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(produit.nom, fontWeight = FontWeight.Bold)
                                    Text("${produit.prix} FCFA")
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stock: ${produit.quantite}", color = if (produit.quantite <= 5) Color.Red else Color.Gray)
                                    if (produit.code_barre != null) {
                                        Text("CB: ${produit.code_barre}", fontSize = 10.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }

                produitSelectionne?.let { p ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Vente : ${p.nom}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = quantite, onValueChange = { quantite = it }, label = { Text("Qté") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = reduction, onValueChange = { reduction = it }, label = { Text("Réduction (FCFA)") }, modifier = Modifier.weight(1f))
                            }

                            val qte = quantite.toIntOrNull() ?: 0
                            val red = reduction.toDoubleOrNull() ?: 0.0
                            val totalBrut = p.prix * qte
                            val prixFinal = if (totalBrut > 0) totalBrut - red else 0.0

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Brut :")
                                Text("${totalBrut} FCFA")
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PRIX À PAYER :", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("${prixFinal} FCFA", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                            }

                            Button(
                                onClick = {
                                    val qteInt = quantite.toIntOrNull()
                                    if (qteInt == null || qteInt <= 0) {
                                        message = "Quantité invalide"
                                        messageColor = Color.Red
                                        return@Button
                                    }
                                    chargement = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            // Envoyer avec magasin_id
                                            val response = RetrofitClient.instance.enregistrerVente(
                                                session.getBearerToken(),
                                                mapOf(
                                                    "produit_id" to p.id, 
                                                    "quantite" to qteInt,
                                                    "magasin_id" to session.getMagasinId()
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                chargement = false
                                                if (response.isSuccessful) {
                                                    genererFacturePDF(context, p, qteInt, red, prixFinal)
                                                    message = "Vente réussie ! Facture générée."
                                                    messageColor = Color.Green
                                                    quantite = ""
                                                    reduction = "0"
                                                    produitSelectionne = null
                                                } else {
                                                    message = "Échec : Stock insuffisant"
                                                    messageColor = Color.Red
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                chargement = false
                                                message = "Erreur serveur"
                                                messageColor = Color.Red
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !chargement
                            ) {
                                if (chargement) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                else Text("Valider et Imprimer Facture")
                            }
                        }
                    }
                }

                if (message.isNotEmpty()) {
                    Text(message, color = messageColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

fun genererFacturePDF(context: Context, p: Produit, qte: Int, reduction: Double, total: Double) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    paint.isFakeBoldText = true
    paint.textSize = 14f
    canvas.drawText("FACTURE - GESTION MAGASIN", 50f, 40f, paint)
    
    paint.isFakeBoldText = false
    paint.textSize = 10f
    canvas.drawText("Date : $date", 20f, 70f, paint)
    canvas.drawText("-------------------------------------------", 20f, 90f, paint)
    
    canvas.drawText("Produit : ${p.nom}", 20f, 110f, paint)
    canvas.drawText("Quantité : $qte", 20f, 130f, paint)
    canvas.drawText("Prix Unitaire : ${p.prix} FCFA", 20f, 150f, paint)
    canvas.drawText("Réduction : $reduction FCFA", 20f, 170f, paint)
    
    paint.isFakeBoldText = true
    paint.textSize = 12f
    canvas.drawText("TOTAL PAYÉ : $total FCFA", 20f, 210f, paint)
    
    paint.isFakeBoldText = false
    paint.textSize = 8f
    canvas.drawText("Merci de votre confiance !", 80f, 250f, paint)

    pdfDocument.finishPage(page)

    val fileName = "Facture_${System.currentTimeMillis()}.pdf"
    val file = File(context.getExternalFilesDir(null), fileName)
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "Facture enregistrée : ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur PDF", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
}
