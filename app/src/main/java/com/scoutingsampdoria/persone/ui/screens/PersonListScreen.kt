package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone.BuildConfig
import com.scoutingsampdoria.persone.R
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonListScreen(
    viewModel: PersoneViewModel,
    ruoloUtente: String?,
    onPersonaClick: (Int) -> Unit,
    onNuovaPersona: () -> Unit,
    onConfigurazione: () -> Unit,
    onIndietro: () -> Unit,
    onLogout: () -> Unit
) {
    var ricerca by remember { mutableStateOf("") }
    var pannelloFiltriAperto by remember { mutableStateOf(false) }
    val isAdmin = ruoloUtente == "admin"
    val puoInserire = ruoloUtente == "admin" || ruoloUtente == "editor"

    LaunchedEffect(Unit) {
        viewModel.caricaLista()
        viewModel.caricaCampiCustom()
    }

    val filtriAttivi = listOfNotNull(
        viewModel.filtroRegione,
        viewModel.filtroRuolo,
        viewModel.filtroSocieta,
        viewModel.filtroQuickReport
    ).size + viewModel.filtriExtra.size

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sampdoria),
                                contentDescription = "Logo U.C. Sampdoria",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scouting Sampdoria",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${viewModel.totale} giocatori · v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onIndietro) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Torna alla home",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        if (isAdmin) {
                            var menuImpostazioniAperto by remember { mutableStateOf(false) }
                            var mostraDialogHelp by remember { mutableStateOf(false) }

                            Box {
                                IconButton(onClick = { menuImpostazioniAperto = true }) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "Impostazioni",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuImpostazioniAperto,
                                    onDismissRequest = { menuImpostazioniAperto = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Configurazione") },
                                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                        onClick = {
                                            menuImpostazioniAperto = false
                                            onConfigurazione()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Help") },
                                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                                        onClick = {
                                            menuImpostazioniAperto = false
                                            mostraDialogHelp = true
                                        }
                                    )
                                }
                            }

                            if (mostraDialogHelp) {
                                AlertDialog(
                                    onDismissRequest = { mostraDialogHelp = false },
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Info, contentDescription = null, tint = SampColors.Blu)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Informazioni", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    text = {
                                        Column {
                                            Text(
                                                "Scouting Sampdoria",
                                                fontWeight = FontWeight.Bold,
                                                color = SampColors.Blu,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Versione ${BuildConfig.VERSION_NAME}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                "App sviluppata da Di Vito Ruggero",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { mostraDialogHelp = false }) { Text("Chiudi") }
                                    }
                                )
                            }
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.Filled.Logout,
                                contentDescription = "Esci",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
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
        floatingActionButton = {
            if (puoInserire) {
                FloatingActionButton(
                    onClick = onNuovaPersona,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Aggiungi giocatore")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ----- Barra ricerca + filtri -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ricerca,
                    onValueChange = {
                        ricerca = it
                        viewModel.caricaLista(query = it)
                    },
                    placeholder = { Text("Cerca giocatore...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SampColors.Blu,
                        unfocusedBorderColor = SampColors.Divisore
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { pannelloFiltriAperto = !pannelloFiltriAperto },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (filtriAttivi > 0) SampColors.Rosso else MaterialTheme.colorScheme.surface,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filtri",
                            tint = if (filtriAttivi > 0) Color.White else SampColors.Blu
                        )
                    }
                    if (filtriAttivi > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .background(SampColors.Blu, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filtriAttivi.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ----- Card Panoramica scouting -----
            CardPanoramica(persone = viewModel.persone, totale = viewModel.totale)

            // ----- Tab categorie scorrevoli -----
            TabCategorieOrizzontali(viewModel = viewModel)

            // ----- Pannello filtri (espandibile) -----
            if (pannelloFiltriAperto) {
                PannelloFiltri(viewModel)
            }

            when {
                viewModel.caricamento -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                viewModel.errore != null -> {
                    Text(
                        text = viewModel.errore ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                viewModel.persone.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nessun giocatore trovato",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (filtriAttivi > 0 || ricerca.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Prova a ridurre i filtri",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(viewModel.persone) { persona ->
                            SchedaGiocatore(persona = persona, onClick = { onPersonaClick(persona.id) })
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card "Panoramica scouting" con 4 statistiche a colpo d'occhio:
 * Totali, Da vedere, Seguire, Monitorare.
 */
@Composable
private fun CardPanoramica(persone: List<Persona>, totale: Int) {
    val daVedere = persone.count { it.quickReport?.contains("vedere", ignoreCase = true) == true }
    val seguire = persone.count { it.quickReport?.contains("seguire", ignoreCase = true) == true }
    val monitorare = persone.count { it.quickReport?.contains("monitor", ignoreCase = true) == true }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp)) {
            Text(
                text = "PANORAMICA SCOUTING",
                style = MaterialTheme.typography.labelSmall,
                color = SampColors.Blu,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox(numero = totale, etichetta = "Totali", colore = SampColors.Blu)
                StatBox(numero = daVedere, etichetta = "Da vedere", colore = SampColors.Rosso)
                StatBox(numero = seguire, etichetta = "Seguire", colore = SampColors.Warning)
                StatBox(numero = monitorare, etichetta = "Monitor.", colore = SampColors.Success)
            }
        }
    }
}

@Composable
private fun StatBox(numero: Int, etichetta: String, colore: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = numero.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = colore,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = etichetta,
            style = MaterialTheme.typography.labelSmall,
            color = SampColors.TestoSecondario
        )
    }
}

/**
 * Riga scorrevole di chip categoria (Tutti + una per ogni valore di CATEGORIA
 * presente nei dati). Cliccando applica/rimuove il filtro categoria.
 */
@Composable
private fun TabCategorieOrizzontali(viewModel: PersoneViewModel) {
    val categorie = viewModel.valoriExtra["CATEGORIA"].orEmpty()
    if (categorie.isEmpty()) return

    val categoriaSelezionata = viewModel.filtriExtra["CATEGORIA"]
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Chip "Tutti"
        ChipCategoria(
            etichetta = "Tutti",
            contatore = viewModel.totale,
            selezionato = categoriaSelezionata == null,
            onClick = { viewModel.impostaFiltroExtra("CATEGORIA", null) }
        )
        categorie.forEach { cat ->
            val contatore = viewModel.persone.count { it.extra?.get("CATEGORIA") == cat }
            ChipCategoria(
                etichetta = cat,
                contatore = if (categoriaSelezionata == cat) viewModel.totale else contatore,
                selezionato = categoriaSelezionata == cat,
                onClick = {
                    if (categoriaSelezionata == cat) viewModel.impostaFiltroExtra("CATEGORIA", null)
                    else viewModel.impostaFiltroExtra("CATEGORIA", cat)
                }
            )
        }
    }
}

@Composable
private fun ChipCategoria(etichetta: String, contatore: Int, selezionato: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selezionato) SampColors.Blu else SampColors.Superficie)
            .then(if (!selezionato) Modifier.background(color = SampColors.Superficie) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etichetta,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selezionato) Color.White else SampColors.Nero
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = contatore.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selezionato) Color.White.copy(alpha = 0.85f) else SampColors.TestoMuto
        )
    }
}

@Composable
private fun PannelloFiltri(viewModel: PersoneViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtri avanzati", fontWeight = FontWeight.Bold, color = SampColors.Blu)
                Text(
                    "Azzera tutti",
                    style = MaterialTheme.typography.labelMedium,
                    color = SampColors.Rosso,
                    modifier = Modifier.clickable { viewModel.azzeraFiltri() }
                )
            }
            Spacer(Modifier.height(8.dp))

            FiltroMenu("Regione", viewModel.filtroRegione, viewModel.valoriRegione) {
                viewModel.impostaFiltro(regione = it)
            }
            FiltroMenu("Ruolo", viewModel.filtroRuolo, viewModel.valoriRuolo) {
                viewModel.impostaFiltro(ruolo = it)
            }
            FiltroMenu("Società", viewModel.filtroSocieta, viewModel.valoriSocieta) {
                viewModel.impostaFiltro(societa = it)
            }
            FiltroMenu("Quick report", viewModel.filtroQuickReport, viewModel.valoriQuickReport) {
                viewModel.impostaFiltro(quickReport = it)
            }
            // Escludo CATEGORIA (già gestita dai tab in alto)
            viewModel.campiCustom.filter { it.nome != "CATEGORIA" }.forEach { campo ->
                val valori = viewModel.valoriExtra[campo.nome].orEmpty()
                if (valori.isNotEmpty()) {
                    FiltroMenu(
                        etichetta = campo.nome,
                        valoreSelezionato = viewModel.filtriExtra[campo.nome],
                        valoriDisponibili = valori,
                        onSeleziona = { viewModel.impostaFiltroExtra(campo.nome, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltroMenu(
    etichetta: String,
    valoreSelezionato: String?,
    valoriDisponibili: List<String>,
    onSeleziona: (String?) -> Unit
) {
    if (valoriDisponibili.isEmpty()) return
    var menuAperto by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            etichetta,
            modifier = Modifier.width(110.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.weight(1f)) {
            AssistChip(
                onClick = { menuAperto = true },
                label = { Text(valoreSelezionato ?: "Tutti") },
                trailingIcon = {
                    if (valoreSelezionato != null) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "Rimuovi",
                            modifier = Modifier.size(16.dp).clickable { onSeleziona(null) }
                        )
                    } else {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = if (valoreSelezionato != null)
                    AssistChipDefaults.assistChipColors(
                        containerColor = SampColors.BluNebbia,
                        labelColor = SampColors.Blu
                    )
                else AssistChipDefaults.assistChipColors()
            )
            DropdownMenu(expanded = menuAperto, onDismissRequest = { menuAperto = false }) {
                DropdownMenuItem(
                    text = { Text("Tutti", fontWeight = FontWeight.Bold) },
                    onClick = {
                        onSeleziona(null)
                        menuAperto = false
                    }
                )
                valoriDisponibili.forEach { valore ->
                    DropdownMenuItem(
                        text = { Text(valore) },
                        onClick = {
                            onSeleziona(valore)
                            menuAperto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FasciaBlucerchiata(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().height(4.dp)) {
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Bianco))
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Rosso))
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Nero))
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Bianco))
    }
}

@Composable
private fun SchedaGiocatore(persona: Persona, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.Superficie),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar iniziali
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SampColors.Blu, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val iniziali = "${persona.nome.firstOrNull() ?: ""}${persona.cognome.firstOrNull() ?: ""}"
                Text(
                    text = iniziali.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${persona.cognome} ${persona.nome}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SampColors.Nero
                )
                Text(
                    text = listOfNotNull(persona.societa, persona.regione)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                        .ifBlank { "-" },
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario
                )
                val dataNascita = persona.dataNascita?.let { formattaDataItaliana(it) }
                val categoria = persona.extra?.entries?.firstOrNull {
                    it.key.equals("CATEGORIA", ignoreCase = true) ||
                    it.key.equals("CAT", ignoreCase = true)
                }?.value
                if (dataNascita != null || categoria != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dataNascita != null) {
                            Icon(
                                Icons.Filled.Cake,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = SampColors.TestoSecondario
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = dataNascita,
                                style = MaterialTheme.typography.labelSmall,
                                color = SampColors.TestoSecondario
                            )
                        }
                        if (dataNascita != null && categoria != null) {
                            Spacer(Modifier.width(8.dp))
                        }
                        if (categoria != null) {
                            Box(
                                modifier = Modifier
                                    .background(SampColors.BluNebbia, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = categoria,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SampColors.Blu,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            persona.ruolo?.takeIf { it.isNotBlank() }?.let { ruolo ->
                Box(
                    modifier = Modifier
                        .background(SampColors.Rosso, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = ruolo,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formattaDataItaliana(iso: String): String {
    return try {
        val parts = iso.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
    } catch (e: Exception) {
        iso
    }
}
