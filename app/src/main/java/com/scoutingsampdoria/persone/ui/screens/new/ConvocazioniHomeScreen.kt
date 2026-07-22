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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.ConvocazioniViewModel
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocazioniHomeScreen(
    personeViewModel: PersoneViewModel,
    convocazioniViewModel: ConvocazioniViewModel,
    onIndietro: () -> Unit,
    onCategoriaSelezionata: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        // Carica lista giocatori completa (senza filtri) per estrarre categorie
        personeViewModel.caricaLista()
    }

    // Ricalcola categorie quando la lista giocatori cambia
    LaunchedEffect(personeViewModel.persone.size) {
        convocazioniViewModel.caricaCategorieDaPersone(personeViewModel.persone)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Convocazioni", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onIndietro) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro", tint = MaterialTheme.colorScheme.onPrimary)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Card intro
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELEZIONA CATEGORIA",
                        style = MaterialTheme.typography.labelSmall,
                        color = SampColors.Blu,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Scegli la categoria per gestire le convocazioni",
                        style = MaterialTheme.typography.bodySmall,
                        color = SampColors.TestoSecondario
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                personeViewModel.caricamento -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                convocazioniViewModel.categorieDisponibili.isEmpty() -> {
                    Text(
                        "Nessuna categoria disponibile. Assicurati che i giocatori nel database abbiano una CATEGORIA assegnata.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SampColors.TestoSecondario,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    convocazioniViewModel.categorieDisponibili.forEach { categoria ->
                        val conteggio = personeViewModel.persone.count { it.extra?.get("CATEGORIA") == categoria }
                        PulsanteCategoria(
                            categoria = categoria,
                            numeroGiocatori = conteggio,
                            onClick = { onCategoriaSelezionata(categoria) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsanteCategoria(
    categoria: String,
    numeroGiocatori: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.Superficie),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SampColors.Rosso.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EventNote,
                    contentDescription = null,
                    tint = SampColors.Rosso,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoria,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SampColors.Nero
                )
                Text(
                    text = "$numeroGiocatori giocatori disponibili",
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SampColors.TestoMuto
            )
        }
    }
}
