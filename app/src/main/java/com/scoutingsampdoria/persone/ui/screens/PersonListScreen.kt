package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    val isAdmin = ruoloUtente == "admin"
    val puoInserire = ruoloUtente == "admin" || ruoloUtente == "editor"

    LaunchedEffect(Unit) {
        viewModel.caricaLista()
        viewModel.caricaCampiCustom()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    // Logo Sampdoria a sinistra
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.logo_sampdoria),
                            contentDescription = "Logo U.C. Sampdoria",
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(40.dp)
                        )
                    },
                    // Titolo centrato con contatore sotto
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
                                text = "${viewModel.totale} giocatori",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    actions = {
                        if (isAdmin) {
                            IconButton(onClick = onConfigurazione) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Configurazione",
                                    tint = MaterialTheme.colorScheme.onPrimary
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
                // Fascia blucerchiata: bianco-rosso-nero sotto la barra
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

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
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
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
                    .size(48.dp)
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
                Text(
                    text = listOfNotNull(persona.societa, persona.regione)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                        .ifBlank { "-" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Badge ruolo (es. GDM, POR)
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
