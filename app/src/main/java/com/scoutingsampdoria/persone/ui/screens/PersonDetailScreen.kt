package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personaId: Int,
    viewModel: PersoneViewModel,
    ruoloUtente: String?,
    onIndietro: () -> Unit,
    onModifica: (Int) -> Unit,
    onEliminato: () -> Unit
) {
    var mostraConfermaElimina by remember { mutableStateOf(false) }
    var tabSelezionato by remember { mutableStateOf(0) }

    LaunchedEffect(personaId) {
        viewModel.caricaDettaglio(personaId)
        viewModel.caricaCampiCustom()
    }

    val puoModificare = ruoloUtente == "admin" || ruoloUtente == "editor"
    val puoEliminare = ruoloUtente == "admin"

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Scheda giocatore", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onIndietro) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    actions = {
                        if (puoModificare) {
                            IconButton(onClick = { onModifica(personaId) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        if (puoEliminare) {
                            IconButton(onClick = { mostraConfermaElimina = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.onPrimary)
                            }
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
        when {
            viewModel.caricamento -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            viewModel.errore != null -> {
                Text(
                    text = viewModel.errore ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(padding).padding(16.dp)
                )
            }
            else -> {
                val persona = viewModel.personaSelezionata
                if (persona != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ---- Hero blucerchiato ----
                        HeroGiocatore(persona)

                        // ---- Tab ----
                        TabInfo(
                            tabSelezionato = tabSelezionato,
                            onTabCambiato = { tabSelezionato = it }
                        )

                        // ---- Contenuto tab ----
                        Box(modifier = Modifier.padding(16.dp)) {
                            when (tabSelezionato) {
                                0 -> SezioneInfo(persona)
                                1 -> SezioneCampiCustom(persona, viewModel)
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (mostraConfermaElimina) {
        AlertDialog(
            onDismissRequest = { mostraConfermaElimina = false },
            title = { Text("Eliminare questo giocatore?") },
            text = { Text("La scheda verrà eliminata definitivamente dal database.") },
            confirmButton = {
                TextButton(onClick = {
                    mostraConfermaElimina = false
                    viewModel.eliminaPersona(personaId, onEliminato)
                }) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraConfermaElimina = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

/**
 * Header con sfondo blucerchiato, avatar grande, nome, ruolo e categoria.
 */
@Composable
private fun HeroGiocatore(persona: Persona) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SampColors.Blu)
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar grande
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${persona.nome.firstOrNull() ?: ""}${persona.cognome.firstOrNull() ?: ""}".uppercase(),
                    color = SampColors.Blu,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${persona.cognome} ${persona.nome}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            val ruoloTesto = persona.ruolo?.takeIf { it.isNotBlank() } ?: "Nessun ruolo"
            val categoria = persona.extra?.get("CATEGORIA")
            val sottotitolo = if (categoria != null) "$ruoloTesto · $categoria" else ruoloTesto
            Text(
                text = sottotitolo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))

            // Riga: rating stellina + badge quick report
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RatingStelline(persona.extra?.get("RATING")?.toIntOrNull() ?: 0)
                persona.quickReport?.takeIf { it.isNotBlank() }?.let { qr ->
                    Box(
                        modifier = Modifier
                            .background(SampColors.Rosso, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = qr.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    FasciaBlucerchiata()
}

@Composable
private fun RatingStelline(livello: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= livello) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= livello) SampColors.Warning else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TabInfo(tabSelezionato: Int, onTabCambiato: (Int) -> Unit) {
    val tabs = listOf("Info", "Campi custom")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SampColors.Superficie)
    ) {
        tabs.forEachIndexed { indice, titolo ->
            val selezionato = tabSelezionato == indice
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabCambiato(indice) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titolo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selezionato) FontWeight.Bold else FontWeight.Medium,
                    color = if (selezionato) SampColors.Blu else SampColors.TestoSecondario
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp)
                        .background(if (selezionato) SampColors.Blu else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SezioneInfo(persona: Persona) {
    Column {
        // Griglia 2x2 di card info
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardInfo(
                icona = Icons.Filled.Cake,
                etichetta = "Nascita",
                valore = persona.dataNascita?.let { formattaData(it) } ?: "-",
                modifier = Modifier.weight(1f)
            )
            CardInfo(
                icona = Icons.Filled.LocationOn,
                etichetta = "Regione",
                valore = persona.regione ?: "-",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardInfo(
                icona = Icons.Filled.SportsSoccer,
                etichetta = "Società",
                valore = persona.societa ?: "-",
                modifier = Modifier.weight(1f)
            )
            CardInfo(
                icona = Icons.Filled.Star,
                etichetta = "Ruolo",
                valore = persona.ruolo ?: "-",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        // Matricola su riga larga
        if (persona.matricola?.isNotBlank() == true) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SampColors.SuperficieAlt),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "MATRICOLA",
                        style = MaterialTheme.typography.labelSmall,
                        color = SampColors.TestoSecondario
                    )
                    Text(
                        persona.matricola,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CardInfo(icona: ImageVector, etichetta: String, valore: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.SuperficieAlt),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icona, contentDescription = null, tint = SampColors.Blu, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    etichetta.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SampColors.TestoSecondario
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = valore,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SampColors.Nero
            )
        }
    }
}

@Composable
private fun SezioneCampiCustom(persona: Persona, viewModel: PersoneViewModel) {
    if (viewModel.campiCustom.isEmpty()) {
        Text(
            "Nessun campo personalizzato configurato.",
            style = MaterialTheme.typography.bodyMedium,
            color = SampColors.TestoSecondario
        )
        return
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            viewModel.campiCustom.forEachIndexed { indice, campo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = campo.nome,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SampColors.TestoSecondario,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = persona.extra?.get(campo.nome)?.takeIf { it.isNotBlank() } ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SampColors.Nero
                    )
                }
                if (indice < viewModel.campiCustom.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(1.dp)
                            .background(SampColors.Divisore)
                    )
                }
            }
        }
    }
}

private fun formattaData(iso: String): String {
    return try {
        val parts = iso.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
    } catch (e: Exception) {
        iso
    }
}
