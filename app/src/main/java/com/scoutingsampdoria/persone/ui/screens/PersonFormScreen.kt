package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone.data.model.PersonaRequest
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormScreen(
    personaId: Int?,
    viewModel: PersoneViewModel,
    onIndietro: () -> Unit,
    onSalvato: () -> Unit
) {
    var cognome by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var societa by remember { mutableStateOf("") }
    var dataNascita by remember { mutableStateOf("") }
    var regione by remember { mutableStateOf("") }
    var ruolo by remember { mutableStateOf("") }
    var matricola by remember { mutableStateOf("") }
    var quickReport by remember { mutableStateOf("") }
    val valoriExtra = remember { mutableStateMapOf<String, String>() }

    val inModifica = personaId != null

    LaunchedEffect(personaId) {
        viewModel.caricaCampiCustom()
        if (personaId != null) {
            viewModel.caricaDettaglio(personaId)
        }
    }

    LaunchedEffect(viewModel.personaSelezionata) {
        viewModel.personaSelezionata?.let { p ->
            if (inModifica) {
                cognome = p.cognome
                nome = p.nome
                societa = p.societa ?: ""
                dataNascita = p.dataNascita ?: ""
                regione = p.regione ?: ""
                ruolo = p.ruolo ?: ""
                matricola = p.matricola ?: ""
                quickReport = p.quickReport ?: ""
                p.extra?.forEach { (k, v) -> valoriExtra[k] = v }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inModifica) "Modifica giocatore" else "Nuovo giocatore", fontWeight = FontWeight.Bold) },
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dati anagrafici", fontWeight = FontWeight.Bold, color = SampColors.Blu)
                    Spacer(Modifier.height(8.dp))
                    CampoTesto("Cognome *", cognome) { cognome = it }
                    CampoTesto("Nome *", nome) { nome = it }
                    CampoTesto("Società", societa) { societa = it }
                    CampoTesto("Data di nascita (YYYY-MM-DD)", dataNascita) { dataNascita = it }
                    CampoTesto("Regione", regione) { regione = it }
                    CampoTesto("Ruolo", ruolo) { ruolo = it }
                    CampoTesto("Matricola", matricola) { matricola = it }
                    CampoTesto("Quick report", quickReport) { quickReport = it }
                }
            }

            // Campi personalizzati (se presenti)
            if (viewModel.campiCustom.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Campi personalizzati", fontWeight = FontWeight.Bold, color = SampColors.Blu)
                        Spacer(Modifier.height(8.dp))
                        viewModel.campiCustom.forEach { campo ->
                            CampoTesto(
                                etichetta = campo.nome,
                                valore = valoriExtra[campo.nome] ?: ""
                            ) { valoriExtra[campo.nome] = it }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (viewModel.caricamento) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = {
                        if (cognome.isBlank() || nome.isBlank()) return@Button
                        val extraPulito = valoriExtra
                            .filterValues { it.isNotBlank() }
                            .toMap()
                        val richiesta = PersonaRequest(
                            cognome = cognome.trim(),
                            nome = nome.trim(),
                            societa = societa.trim().takeIf { it.isNotBlank() },
                            dataNascita = dataNascita.trim().takeIf { it.isNotBlank() },
                            regione = regione.trim().takeIf { it.isNotBlank() },
                            ruolo = ruolo.trim().takeIf { it.isNotBlank() },
                            matricola = matricola.trim().takeIf { it.isNotBlank() },
                            quickReport = quickReport.trim().takeIf { it.isNotBlank() },
                            extra = extraPulito
                        )
                        viewModel.salvaPersona(personaId, richiesta, onSalvato)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (inModifica) "Salva modifiche" else "Crea giocatore")
                }
            }

            viewModel.errore?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun CampoTesto(etichetta: String, valore: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valore,
        onValueChange = onChange,
        label = { Text(etichetta) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
