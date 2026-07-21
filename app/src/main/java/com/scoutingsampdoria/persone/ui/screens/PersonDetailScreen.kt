package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Intestazione con avatar grande
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(SampColors.Blu, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${persona.nome.firstOrNull() ?: ""}${persona.cognome.firstOrNull() ?: ""}".uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "${persona.cognome} ${persona.nome}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                persona.ruolo?.takeIf { it.isNotBlank() }?.let { ruolo ->
                                    Spacer(Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(SampColors.Rosso, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(ruolo, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Dati anagrafici
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Dati anagrafici",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SampColors.Blu
                                )
                                Spacer(Modifier.height(8.dp))
                                RigaDettaglio("Società", persona.societa)
                                RigaDettaglio("Data di nascita", persona.dataNascita)
                                RigaDettaglio("Regione", persona.regione)
                                RigaDettaglio("Matricola", persona.matricola)
                                RigaDettaglio("Quick report", persona.quickReport)
                            }
                        }

                        // Campi personalizzati (sempre tutti mostrati, "-" se vuoti)
                        if (viewModel.campiCustom.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Campi personalizzati",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SampColors.Blu
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    viewModel.campiCustom.forEach { campo ->
                                        RigaDettaglio(campo.nome, persona.extra?.get(campo.nome))
                                    }
                                }
                            }
                        }
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

@Composable
private fun RigaDettaglio(etichetta: String, valore: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etichetta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valore?.takeIf { it.isNotBlank() } ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}
