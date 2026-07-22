package com.scoutingsampdoria.persone.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scoutingsampdoria.persone.data.model.Convocazione
import com.scoutingsampdoria.persone.data.model.ConvocazioneGiocatore
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.ConvocazioniViewModel
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.FileProvider

private val MODULI_DISPONIBILI = listOf(
    "4-4-2", "4-3-3", "4-2-3-1", "3-5-2", "3-4-3", "4-3-1-2", "4-4-1-1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocazioneDetailScreen(
    convocazioneId: Int,
    categoria: String,
    viewModel: ConvocazioniViewModel,
    personeViewModel: PersoneViewModel,
    onIndietro: () -> Unit
) {
    val context = LocalContext.current
    var tabSelezionato by remember { mutableStateOf(0) }

    LaunchedEffect(convocazioneId) {
        viewModel.caricaDettaglioConvocazione(convocazioneId)
        if (personeViewModel.persone.isEmpty()) {
            personeViewModel.caricaLista()
        }
    }

    LaunchedEffect(personeViewModel.persone.size, categoria) {
        viewModel.caricaGiocatoriCategoria(personeViewModel.persone, categoria)
    }

    val conv = viewModel.convocazioneCorrente

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = conv?.let { "${it.squadraCasa ?: "Casa"} vs ${it.squadraOspite ?: "Ospite"}" }
                                    ?: "Convocazione",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                categoria,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onIndietro) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.esportaConvocazionePdf(convocazioneId) { bytes ->
                                condividiPdf(context, bytes, "distinta_$categoria.pdf")
                            }
                        }) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = "Esporta PDF",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FasciaBlucerchiata()
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (viewModel.caricamento && conv == null) {
            Row(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }
        if (conv == null) return@Scaffold

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab
            Row(modifier = Modifier.fillMaxWidth().background(SampColors.Superficie)) {
                listOf("Convocati", "Distinta", "Campo").forEachIndexed { i, titolo ->
                    val sel = tabSelezionato == i
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { tabSelezionato = i }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = titolo,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                            color = if (sel) SampColors.Blu else SampColors.TestoSecondario
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.dp)
                                .background(if (sel) SampColors.Blu else Color.Transparent)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (tabSelezionato) {
                    0 -> TabConvocati(conv, viewModel, personeViewModel)
                    1 -> TabDistinta(conv, viewModel)
                    2 -> TabCampo(conv)
                }
            }
        }
    }
}

// ------------------- TAB CONVOCATI -------------------
@Composable
private fun TabConvocati(
    convocazione: Convocazione,
    viewModel: ConvocazioniViewModel,
    personeViewModel: PersoneViewModel
) {
    val giocatoriIniziali = convocazione.giocatori.orEmpty()
    // Stato locale editabile
    var caselle by remember(convocazione.id) {
        mutableStateOf(giocatoriIniziali.toMutableList())
    }
    var contatoreCambi by remember { mutableStateOf(0) }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        Text(
            text = "GIOCATORI CONVOCATI (${caselle.count { it.personaId != null }}/${caselle.size})",
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.Blu,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        caselle.forEachIndexed { idx, casella ->
            RigaConvocato(
                indice = idx,
                casella = casella,
                giocatoriDisponibili = viewModel.giocatoriCategoria,
                giocatoriGiaScelti = caselle.mapNotNull { it.personaId }.toSet() - setOfNotNull(casella.personaId),
                onGiocatoreScelto = { persona ->
                    caselle = caselle.toMutableList().apply {
                        this[idx] = casella.copy(
                            personaId = persona?.id,
                            cognome = persona?.cognome,
                            nome = persona?.nome,
                            ruolo = persona?.ruolo
                        )
                    }
                    contatoreCambi++
                },
                onElimina = if (caselle.size > 1) {
                    {
                        caselle = caselle.toMutableList().apply { removeAt(idx) }
                            .mapIndexed { i, c -> c.copy(ordine = i) }.toMutableList()
                        contatoreCambi++
                    }
                } else null
            )
        }

        Spacer(Modifier.height(12.dp))

        // Aggiungi casella
        Button(
            onClick = {
                caselle = (caselle + ConvocazioneGiocatore(
                    personaId = null, numero = null, ordine = caselle.size
                )).toMutableList()
                contatoreCambi++
            },
            colors = ButtonDefaults.buttonColors(containerColor = SampColors.BluNebbia, contentColor = SampColors.Blu),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Aggiungi casella")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.aggiornaGiocatori(convocazione.id, caselle.toList()) { }
            },
            enabled = contatoreCambi > 0 && !viewModel.caricamento,
            colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Salva convocati", color = Color.White)
        }

        viewModel.messaggio?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = SampColors.Success, style = MaterialTheme.typography.bodySmall)
        }
        viewModel.errore?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ------------------- riga singola convocato -------------------
@Composable
private fun RigaConvocato(
    indice: Int,
    casella: ConvocazioneGiocatore,
    giocatoriDisponibili: List<Persona>,
    giocatoriGiaScelti: Set<Int>,
    onGiocatoreScelto: (Persona?) -> Unit,
    onElimina: (() -> Unit)?
) {
    var menuAperto by remember { mutableStateOf(false) }
    val giocatoriFiltrati = giocatoriDisponibili.filter { it.id !in giocatoriGiaScelti }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numero casella
            Box(
                modifier = Modifier.size(28.dp).background(SampColors.BluNebbia, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${indice + 1}",
                    fontWeight = FontWeight.Bold,
                    color = SampColors.Blu,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(10.dp))

            // Dropdown giocatore
            Box(modifier = Modifier.weight(1f)) {
                AssistChip(
                    onClick = { menuAperto = true },
                    label = {
                        val etichetta = if (casella.cognome != null && casella.nome != null)
                            "${casella.cognome} ${casella.nome}"
                        else "Seleziona giocatore..."
                        Text(etichetta, maxLines = 1)
                    },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    colors = if (casella.personaId != null)
                        AssistChipDefaults.assistChipColors(
                            containerColor = SampColors.BluNebbia,
                            labelColor = SampColors.Blu
                        )
                    else AssistChipDefaults.assistChipColors()
                )
                DropdownMenu(
                    expanded = menuAperto,
                    onDismissRequest = { menuAperto = false }
                ) {
                    if (casella.personaId != null) {
                        DropdownMenuItem(
                            text = { Text("— Rimuovi selezione —", fontWeight = FontWeight.Bold) },
                            onClick = {
                                onGiocatoreScelto(null)
                                menuAperto = false
                            }
                        )
                    }
                    giocatoriFiltrati.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.cognome} ${p.nome}") },
                            onClick = {
                                onGiocatoreScelto(p)
                                menuAperto = false
                            }
                        )
                    }
                }
            }

            // Elimina casella
            onElimina?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina casella",
                        tint = SampColors.Rosso, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ------------------- TAB DISTINTA -------------------
@Composable
private fun TabDistinta(convocazione: Convocazione, viewModel: ConvocazioniViewModel) {
    val giocatoriConvocati = convocazione.giocatori.orEmpty().filter { it.personaId != null }
    var moduloScelto by remember(convocazione.id) { mutableStateOf(convocazione.modulo ?: "4-4-2") }
    var numeriPerCasella by remember(convocazione.id) {
        mutableStateOf(giocatoriConvocati.associate { it.id!! to (it.numero?.toString() ?: "") }.toMutableMap())
    }
    var moduloMenuAperto by remember { mutableStateOf(false) }
    var contatoreCambi by remember { mutableStateOf(0) }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        Text(
            text = "MODULO",
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.Blu,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        Box {
            AssistChip(
                onClick = { moduloMenuAperto = true },
                label = { Text("Modulo: $moduloScelto") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = SampColors.BluNebbia,
                    labelColor = SampColors.Blu
                )
            )
            DropdownMenu(expanded = moduloMenuAperto, onDismissRequest = { moduloMenuAperto = false }) {
                MODULI_DISPONIBILI.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m) },
                        onClick = {
                            moduloScelto = m
                            moduloMenuAperto = false
                            contatoreCambi++
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "NUMERI DI MAGLIA",
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.Blu,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "1=POR, 2-6=DIF, 7-8/10-11=CEN, 9-20=ATT",
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.TestoSecondario
        )
        Spacer(Modifier.height(8.dp))

        giocatoriConvocati.forEach { g ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${g.cognome} ${g.nome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = numeriPerCasella[g.id] ?: "",
                    onValueChange = { input ->
                        val filtrato = input.filter { it.isDigit() }.take(2)
                        numeriPerCasella = numeriPerCasella.toMutableMap().apply {
                            this[g.id!!] = filtrato
                        }
                        contatoreCambi++
                    },
                    label = { Text("N°") },
                    singleLine = true,
                    modifier = Modifier.width(90.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // Aggiorna modulo
                viewModel.aggiornaConvocazione(
                    id = convocazione.id,
                    data = convocazione.data,
                    ora = convocazione.ora,
                    impianto = convocazione.impianto,
                    squadraCasa = convocazione.squadraCasa,
                    squadraOspite = convocazione.squadraOspite,
                    modulo = moduloScelto,
                    onCompletato = { }
                )
                // Aggiorna giocatori con nuovi numeri
                val giocatoriConNumeri = convocazione.giocatori.orEmpty().map { g ->
                    if (g.personaId != null) {
                        val num = numeriPerCasella[g.id]?.toIntOrNull()
                        g.copy(numero = num)
                    } else g
                }
                viewModel.aggiornaGiocatori(convocazione.id, giocatoriConNumeri) { }
            },
            enabled = contatoreCambi > 0 && !viewModel.caricamento,
            colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Salva distinta", color = Color.White)
        }

        viewModel.messaggio?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = SampColors.Success, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ------------------- TAB CAMPO -------------------
@Composable
private fun TabCampo(convocazione: Convocazione) {
    val modulo = convocazione.modulo
    val giocatori = convocazione.giocatori.orEmpty()
        .filter { it.personaId != null && it.numero != null }
        .associateBy { it.numero!! }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        if (modulo.isNullOrBlank()) {
            Text(
                "Seleziona prima un modulo nella tab Distinta.",
                style = MaterialTheme.typography.bodyMedium,
                color = SampColors.TestoSecondario
            )
            return@Column
        }

        Text(
            text = "MODULO $modulo",
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.Blu,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        val posizioni = posizioniPerModulo(modulo)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D5B)),
            modifier = Modifier.fillMaxWidth().aspectRatio(0.75f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width
                val h = size.height

                // Linee campo
                drawRect(
                    color = Color.White,
                    topLeft = Offset(0f, 0f),
                    size = Size(w, h),
                    style = Stroke(width = 3f)
                )
                // Linea centrale
                drawLine(
                    color = Color.White,
                    start = Offset(0f, h / 2),
                    end = Offset(w, h / 2),
                    strokeWidth = 3f
                )
                // Cerchio centrale
                drawCircle(
                    color = Color.White,
                    radius = w * 0.13f,
                    center = Offset(w / 2, h / 2),
                    style = Stroke(width = 3f)
                )
                // Aree di rigore
                val areaW = w * 0.55f
                val areaH = h * 0.18f
                drawRect(
                    color = Color.White,
                    topLeft = Offset((w - areaW) / 2, 0f),
                    size = Size(areaW, areaH),
                    style = Stroke(width = 3f)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset((w - areaW) / 2, h - areaH),
                    size = Size(areaW, areaH),
                    style = Stroke(width = 3f)
                )

                // Giocatori
                posizioni.forEach { (numero, coord) ->
                    val (xRel, yRel) = coord
                    val x = xRel * w
                    val y = (1 - yRel) * h  // inverto: 0=basso => attacchiamo verso alto

                    val giocatore = giocatori[numero]
                    val coloreCerchio = if (giocatore != null) Color(0xFF003D7A) else Color(0xFF888888)

                    // Cerchio
                    drawCircle(
                        color = coloreCerchio,
                        radius = w * 0.055f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.055f,
                        center = Offset(x, y),
                        style = Stroke(width = 3f)
                    )

                    // Testo (usiamo native Canvas per il testo)
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = w * 0.05f
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(numero.toString(), x, y + (w * 0.018f), paint)

                        // Cognome sotto
                        if (giocatore != null) {
                            val paintNome = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = w * 0.028f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                            }
                            drawText(
                                (giocatore.cognome ?: "").uppercase().take(10),
                                x,
                                y + w * 0.09f,
                                paintNome
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Posizioni derivate dai numeri di maglia. Grigio = casella vuota.",
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.TestoSecondario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

private fun posizioniPerModulo(modulo: String): Map<Int, Pair<Float, Float>> {
    return when (modulo) {
        "4-4-2" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.85f to 0.22f), 3 to (0.15f to 0.22f), 4 to (0.63f to 0.20f), 5 to (0.37f to 0.20f),
            7 to (0.85f to 0.50f), 8 to (0.60f to 0.48f), 10 to (0.40f to 0.48f), 11 to (0.15f to 0.50f),
            9 to (0.35f to 0.78f), 20 to (0.65f to 0.78f)
        )
        "4-3-3" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.85f to 0.22f), 3 to (0.15f to 0.22f), 4 to (0.63f to 0.20f), 5 to (0.37f to 0.20f),
            8 to (0.5f to 0.44f), 6 to (0.25f to 0.44f), 10 to (0.75f to 0.44f),
            7 to (0.15f to 0.75f), 9 to (0.5f to 0.78f), 11 to (0.85f to 0.75f)
        )
        "4-2-3-1" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.85f to 0.22f), 3 to (0.15f to 0.22f), 4 to (0.63f to 0.20f), 5 to (0.37f to 0.20f),
            6 to (0.35f to 0.40f), 8 to (0.65f to 0.40f),
            10 to (0.5f to 0.60f), 7 to (0.85f to 0.65f), 11 to (0.15f to 0.65f),
            9 to (0.5f to 0.82f)
        )
        "3-5-2" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.80f to 0.22f), 5 to (0.5f to 0.20f), 3 to (0.20f to 0.22f),
            4 to (0.85f to 0.45f), 8 to (0.65f to 0.48f), 6 to (0.5f to 0.48f),
            10 to (0.35f to 0.48f), 11 to (0.15f to 0.45f),
            9 to (0.4f to 0.78f), 20 to (0.6f to 0.78f)
        )
        "3-4-3" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.80f to 0.22f), 5 to (0.5f to 0.20f), 3 to (0.20f to 0.22f),
            7 to (0.85f to 0.48f), 8 to (0.62f to 0.48f), 10 to (0.38f to 0.48f), 11 to (0.15f to 0.48f),
            9 to (0.5f to 0.78f), 20 to (0.25f to 0.75f), 6 to (0.75f to 0.75f)
        )
        "4-3-1-2" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.85f to 0.22f), 3 to (0.15f to 0.22f), 4 to (0.63f to 0.20f), 5 to (0.37f to 0.20f),
            6 to (0.25f to 0.42f), 8 to (0.5f to 0.42f), 7 to (0.75f to 0.42f),
            10 to (0.5f to 0.62f),
            9 to (0.35f to 0.80f), 20 to (0.65f to 0.80f)
        )
        "4-4-1-1" -> mapOf(
            1 to (0.5f to 0.06f),
            2 to (0.85f to 0.22f), 3 to (0.15f to 0.22f), 4 to (0.63f to 0.20f), 5 to (0.37f to 0.20f),
            7 to (0.85f to 0.45f), 8 to (0.63f to 0.45f), 6 to (0.37f to 0.45f), 11 to (0.15f to 0.45f),
            10 to (0.5f to 0.65f),
            9 to (0.5f to 0.82f)
        )
        else -> posizioniPerModulo("4-4-2")
    }
}

private fun condividiPdf(context: android.content.Context, bytes: ByteArray, nomeFile: String) {
    try {
        val cartella = File(context.cacheDir, "pdf_condivisi")
        cartella.mkdirs()
        val file = File(cartella, nomeFile)
        file.writeBytes(bytes)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Condividi PDF"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Errore: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
