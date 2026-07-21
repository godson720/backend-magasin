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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import com.example.gestionmagasin.model.Dashboard
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
fun DashboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    var chargement by remember { mutableStateOf(true) }

    fun chargerDashboard() {
        chargement = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Utilisation du magasin_id sélectionné
                val response = RetrofitClient.instance.getDashboard(
                    session.getBearerToken(),
                    session.getMagasinId()
                )
                withContext(Dispatchers.Main) {
                    chargement = false
                    if (response.isSuccessful) dashboard = response.body()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { chargement = false }
            }
        }
    }

    LaunchedEffect(Unit) { chargerDashboard() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyse Magasin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (dashboard != null) {
                        IconButton(onClick = { exportDashboardToPDF(context, dashboard!!) }) {
                            Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                chargement -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                dashboard == null -> Text("Données indisponibles", modifier = Modifier.align(Alignment.Center))
                else -> {
                    val d = dashboard!!
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard("Stock Total", d.total_produits.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                StatCard("Ventes Jour", d.ventes_jour.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard("CA Magasin", "${d.chiffre_affaires} FCFA", Color(0xFF2196F3), Modifier.weight(1f))
                                StatCard("Ruptures", d.nb_ruptures.toString(), Color.Red, Modifier.weight(1f))
                            }
                        }
                        
                        item { Text("Alerte Stock", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Red) }
                        if (d.produits_rupture.isEmpty()) {
                            item { Text("Tout est en règle", color = Color(0xFF388E3C)) }
                        } else {
                            items(d.produits_rupture) { p ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(p.nom, fontWeight = FontWeight.Bold)
                                        Text("${p.quantite} restants", color = Color.Red)
                                    }
                                }
                            }
                        }

                        item { Text("Produits les plus vendus", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        items(d.top_produits) { tp ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tp.nom)
                                    Text("${tp.total_vendu} vendus", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(titre: String, valeur: String, couleur: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = couleur)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valeur, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(titre, fontSize = 11.sp, color = Color.White)
        }
    }
}

fun exportDashboardToPDF(context: Context, d: Dashboard) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    paint.isFakeBoldText = true
    paint.textSize = 14f
    canvas.drawText("BILAN MAGASIN - GESTION", 40f, 40f, paint)
    
    paint.isFakeBoldText = false
    paint.textSize = 10f
    canvas.drawText("Date : $date", 20f, 70f, paint)
    canvas.drawLine(20f, 80f, 380f, 80f, paint)

    paint.textSize = 11f
    canvas.drawText("- Articles en stock : ${d.total_produits}", 30f, 110f, paint)
    canvas.drawText("- Ventes du jour : ${d.ventes_jour}", 30f, 130f, paint)
    canvas.drawText("- CA du magasin : ${d.chiffre_affaires} FCFA", 30f, 150f, paint)

    pdfDocument.finishPage(page)
    val file = File(context.getExternalFilesDir(null), "Bilan_Magasin_${System.currentTimeMillis()}.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF généré", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
}
