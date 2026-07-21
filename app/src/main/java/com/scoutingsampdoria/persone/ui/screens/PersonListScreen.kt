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
import androidx.compose.material3.FilterChip
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

    // Conteggio filtri attivi (per il badge sull'icona)
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

            // Barra di ricerca + toggle filtri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                // Bottone filtri con badge del numero di filtri attivi
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

            // Pannello filtri (espandibile)
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

@Composable
private fun PannelloFiltri(viewModel: PersoneViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Text("Filtri", fontWeight = FontWeight.Bold, color = SampColors.Blu)
                Text(
                    "Azzera tutti",
                    style = MaterialTheme.typography.labelMedium,
                    color = SampColors.Rosso,
                    modifier = Modifier.clickable { viewModel.azzeraFiltri() }
                )
            }
            Spacer(Modifier.height(8.dp))

            // Campi standard
            FiltroMenu(
                etichetta = "Regione",
                valoreSelezionato = viewModel.filtroRegione,
                valoriDisponibili = viewModel.valoriRegione,
                onSeleziona = { viewModel.impostaFiltro(regione = it) }
            )
            FiltroMenu(
                etichetta = "Ruolo",
                valoreSelezionato = viewModel.filtroRuolo,
                valoriDisponibili = viewModel.valoriRuolo,
                onSeleziona = { viewModel.impostaFiltro(ruolo = it) }
            )
            FiltroMenu(
                etichetta = "Società",
                valoreSelezionato = viewModel.filtroSocieta,
                valoriDisponibili = viewModel.valoriSocieta,
                onSeleziona = { viewModel.impostaFiltro(societa = it) }
            )
            FiltroMenu(
                etichetta = "Quick report",
                valoreSelezionato = viewModel.filtroQuickReport,
                valoriDisponibili = viewModel.valoriQuickReport,
                onSeleziona = { viewModel.impostaFiltro(quickReport = it) }
            )

            // Campi personalizzati
            viewModel.campiCustom.forEach { campo ->
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onSeleziona(null) }
                        )
                    } else {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = if (valoreSelezionato != null)
                    AssistChipDefaults.assistChipColors(
                        containerColor = SampColors.Blu.copy(alpha = 0.12f),
                        labelColor = SampColors.Blu
                    )
                else AssistChipDefaults.assistChipColors()
            )
            DropdownMenu(
                expanded = menuAperto,
                onDismissRequest = { menuAperto = false }
            ) {
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
    Row(modifier = modifier.fillMaxWidth().height(6.dp)) {
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circolare con iniziali
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SampColors.Blu, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val iniziali = "${persona.nome.firstOrNull() ?: ""}${persona.cognome.firstOrNull() ?: ""}"
                Text(
                    text = iniziali.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${persona.cognome} ${persona.nome}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // Riga con società e regione
                Text(
                    text = listOfNotNull(persona.societa, persona.regione)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                        .ifBlank { "-" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Riga aggiuntiva con data di nascita e categoria (dal campo custom 'CATEGORIA' o simile)
                val dataNascita = persona.dataNascita?.let { formattaDataItaliana(it) }
                val categoria = persona.extra?.entries?.firstOrNull {
                    it.key.equals("CATEGORIA", ignoreCase = true) ||
                    it.key.equals("CAT", ignoreCase = true)
                }?.value
                if (dataNascita != null || categoria != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dataNascita != null) {
                            Icon(
                                Icons.Filled.Cake,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = dataNascita,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (dataNascita != null && categoria != null) {
                            Spacer(Modifier.width(12.dp))
                        }
                        if (categoria != null) {
                            Box(
                                modifier = Modifier
                                    .background(SampColors.Blu.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = categoria,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SampColors.Blu,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Badge ruolo (POR, ATT, ecc.)
            persona.ruolo?.takeIf { it.isNotBlank() }?.let { ruolo ->
                Box(
                    modifier = Modifier
                        .background(SampColors.Rosso, RoundedCornerShape(8.dp))
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

/** Converte "2010-05-15" in "15/05/2010" senza dipendenze aggiuntive. */
private fun formattaDataItaliana(iso: String): String {
    return try {
        val parts = iso.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
    } catch (e: Exception) {
        iso
    }
}
