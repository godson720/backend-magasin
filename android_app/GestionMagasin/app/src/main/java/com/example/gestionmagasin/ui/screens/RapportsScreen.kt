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
import androidx.compose.material.icons.filled.DateRange
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
import com.example.gestionmagasin.model.Vente
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
fun RapportsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var ventes by remember { mutableStateOf<List<Vente>>(emptyList()) }
    var chargement by remember { mutableStateOf(true) }
    
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    var moisSelectionne by remember { mutableStateOf(sdf.format(Date())) }

    fun chargerDonnees() {
        chargement = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Utilisation du magasinId de la session pour filtrer les rapports
                val response = RetrofitClient.instance.getVentes(
                    session.getBearerToken(),
                    session.getMagasinId()
                )
                withContext(Dispatchers.Main) {
                    chargement = false
                    if (response.isSuccessful) {
                        ventes = response.body()?.filter { it.date_vente.startsWith(moisSelectionne) } ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { chargement = false }
            }
        }
    }

    LaunchedEffect(moisSelectionne) { chargerDonnees() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapports Périodiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
                },
                actions = {
                    IconButton(onClick = { 
                        if (ventes.isNotEmpty()) {
                            exportRapportPeriodePDF(context, moisSelectionne, ventes)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = Color.White)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Période : ", fontWeight = FontWeight.Bold)
                    Text(moisSelectionne, color = MaterialTheme.colorScheme.primary)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Sorties (Ventes)", fontSize = 12.sp)
                        Text("${ventes.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("${ventes.sumOf { it.montant }} FCFA", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Activités", fontSize = 12.sp)
                        Text("Filtré", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        Text("Par Magasin", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Détail des ventes de la période", fontWeight = FontWeight.Bold)

            Box(modifier = Modifier.fillMaxSize()) {
                if (chargement) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (ventes.isEmpty()) {
                    Text("Aucune donnée pour ce magasin", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(ventes) { vente ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Vente #${vente.id}", fontWeight = FontWeight.Bold)
                                        Text(vente.date_vente, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Text("${vente.montant} FCFA", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun exportRapportPeriodePDF(context: Context, periode: String, ventes: List<Vente>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()

    paint.isFakeBoldText = true
    paint.textSize = 16f
    canvas.drawText("RAPPORT PÉRIODIQUE - $periode", 40f, 40f, paint)
    
    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Total Sorties : ${ventes.sumOf { it.montant }} FCFA", 40f, 70f, paint)
    canvas.drawText("Nombre de ventes : ${ventes.size}", 40f, 90f, paint)
    canvas.drawLine(20f, 110f, 380f, 110f, paint)

    var y = 140f
    ventes.take(20).forEach { v ->
        canvas.drawText("#${v.id} - ${v.date_vente} : ${v.montant} FCFA", 40f, y, paint)
        y += 20f
    }

    pdfDocument.finishPage(page)
    val file = File(context.getExternalFilesDir(null), "Rapport_$periode.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF Exporté", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur PDF", Toast.LENGTH_SHORT).show()
    }
    pdfDocument.close()
}
