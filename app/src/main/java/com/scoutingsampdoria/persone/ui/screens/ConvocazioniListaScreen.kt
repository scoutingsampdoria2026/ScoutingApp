package com.scoutingsampdoria.persone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.scoutingsampdoria.persone.data.model.Convocazione
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.ConvocazioniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocazioniListaScreen(
    categoria: String,
    viewModel: ConvocazioniViewModel,
    onIndietro: () -> Unit,
    onApriConvocazione: (Int) -> Unit
) {
    var mostraDialogNuova by remember { mutableStateOf(false) }
    var convocazioneDaEliminare by remember { mutableStateOf<Convocazione?>(null) }

    LaunchedEffect(categoria) {
        viewModel.caricaConvocazioniPerCategoria(categoria)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Convocazioni", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(categoria, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                    },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostraDialogNuova = true },
                containerColor = SampColors.Rosso,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuova convocazione")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                viewModel.convocazioni.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nessuna convocazione ancora",
                            style = MaterialTheme.typography.titleMedium,
                            color = SampColors.TestoSecondario
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Premi il pulsante + per crearne una nuova",
                            style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoMuto
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(viewModel.convocazioni) { conv ->
                            SchedaConvocazione(
                                convocazione = conv,
                                onClick = { onApriConvocazione(conv.id) },
                                onElimina = { convocazioneDaEliminare = conv }
                            )
                        }
                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    if (mostraDialogNuova) {
        DialogNuovaConvocazione(
            categoria = categoria,
            onAnnulla = { mostraDialogNuova = false },
            onCrea = { data, ora, oraConvocazione, impianto, casa, ospite ->
                mostraDialogNuova = false
                viewModel.creaConvocazione(
                    categoria = categoria,
                    data = data.ifBlank { null },
                    ora = ora.ifBlank { null },
                    oraConvocazione = oraConvocazione.ifBlank { null },
                    impianto = impianto.ifBlank { null },
                    squadraCasa = casa.ifBlank { null },
                    squadraOspite = ospite.ifBlank { null },
                    modulo = null,
                    onCreata = { id -> onApriConvocazione(id) }
                )
            }
        )
    }

    convocazioneDaEliminare?.let { conv ->
        AlertDialog(
            onDismissRequest = { convocazioneDaEliminare = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = SampColors.Rosso)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminare la convocazione?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "${conv.squadraCasa ?: "Squadra casa"} vs ${conv.squadraOspite ?: "Squadra ospite"}",
                        fontWeight = FontWeight.Bold,
                        color = SampColors.Blu
                    )
                    conv.data?.let {
                        Text(
                            formattaDataItaliana(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoSecondario
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "L'operazione è definitiva. La convocazione e tutti i giocatori assegnati verranno rimossi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SampColors.TestoSecondario
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = conv.id
                        convocazioneDaEliminare = null
                        viewModel.eliminaConvocazione(id, categoria) { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.Rosso)
                ) {
                    Text("Elimina", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { convocazioneDaEliminare = null }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun SchedaConvocazione(
    convocazione: Convocazione,
    onClick: () -> Unit,
    onElimina: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Prima riga: squadre
                val casa = convocazione.squadraCasa?.ifBlank { "Squadra casa" } ?: "Squadra casa"
                val ospite = convocazione.squadraOspite?.ifBlank { "Squadra ospite" } ?: "Squadra ospite"
                Text(
                    text = "$casa vs $ospite",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SampColors.Blu
                )
                Spacer(Modifier.height(6.dp))

                // Seconda riga: data + ora + impianto
                Row(verticalAlignment = Alignment.CenterVertically) {
                    convocazione.data?.takeIf { it.isNotBlank() }?.let {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = SampColors.TestoSecondario)
                        Spacer(Modifier.width(4.dp))
                        Text(formattaDataItaliana(it), style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoSecondario)
                        Spacer(Modifier.width(12.dp))
                    }
                    convocazione.ora?.takeIf { it.isNotBlank() }?.let {
                        Icon(Icons.Filled.Schedule, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = SampColors.TestoSecondario)
                        Spacer(Modifier.width(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoSecondario)
                    }
                }

                convocazione.impianto?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = SampColors.TestoSecondario)
                        Spacer(Modifier.width(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoSecondario)
                    }
                }

                convocazione.modulo?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(SampColors.BluNebbia, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Modulo $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = SampColors.Blu,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pulsante elimina
            IconButton(onClick = onElimina) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Elimina convocazione",
                    tint = SampColors.Rosso,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogNuovaConvocazione(
    categoria: String,
    onAnnulla: () -> Unit,
    onCrea: (data: String, ora: String, oraConvocazione: String, impianto: String, casa: String, ospite: String) -> Unit
) {
    var data by remember { mutableStateOf("") }
    var ora by remember { mutableStateOf("") }
    var oraConvocazione by remember { mutableStateOf("") }
    var impianto by remember { mutableStateOf("") }
    var casa by remember { mutableStateOf("Sampdoria $categoria") }
    var ospite by remember { mutableStateOf("") }

    var mostraDatePicker by remember { mutableStateOf(false) }
    var mostraTimePickerPartita by remember { mutableStateOf(false) }
    var mostraTimePickerConvocazione by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerStatePartita = rememberTimePickerState(is24Hour = true)
    val timePickerStateConvocazione = rememberTimePickerState(is24Hour = true)

    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text("Nuova convocazione", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Categoria: $categoria",
                    style = MaterialTheme.typography.labelMedium,
                    color = SampColors.Blu,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = data.ifBlank { "" }.let { if (it.isNotBlank()) formattaDataItaliana(it) else "" },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Data") },
                    placeholder = { Text("Tocca per selezionare") },
                    trailingIcon = {
                        IconButton(onClick = { mostraDatePicker = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Scegli data")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostraDatePicker = true }
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = ora,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Ora partita") },
                    placeholder = { Text("Tocca per selezionare") },
                    trailingIcon = {
                        IconButton(onClick = { mostraTimePickerPartita = true }) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Scegli ora")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostraTimePickerPartita = true }
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = oraConvocazione,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Ora convocazione") },
                    placeholder = { Text("Tocca per selezionare") },
                    trailingIcon = {
                        IconButton(onClick = { mostraTimePickerConvocazione = true }) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Scegli ora convocazione")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostraTimePickerConvocazione = true }
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = impianto,
                    onValueChange = { impianto = titleCase(it) },
                    label = { Text("Impianto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = casa,
                    onValueChange = { casa = titleCase(it) },
                    label = { Text("Squadra casa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ospite,
                    onValueChange = { ospite = titleCase(it) },
                    label = { Text("Squadra ospite") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCrea(data, ora, oraConvocazione, impianto, casa, ospite) },
                colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu)
            ) {
                Text("Crea", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) { Text("Annulla") }
        }
    )

    if (mostraDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostraDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        data = "$y-$m-$d"
                    }
                    mostraDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostraDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostraTimePickerPartita) {
        AlertDialog(
            onDismissRequest = { mostraTimePickerPartita = false },
            title = { Text("Ora partita") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerStatePartita)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerStatePartita.hour.toString().padStart(2, '0')
                    val m = timePickerStatePartita.minute.toString().padStart(2, '0')
                    ora = "$h:$m"
                    mostraTimePickerPartita = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostraTimePickerPartita = false }) { Text("Annulla") }
            }
        )
    }

    if (mostraTimePickerConvocazione) {
        AlertDialog(
            onDismissRequest = { mostraTimePickerConvocazione = false },
            title = { Text("Ora convocazione") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerStateConvocazione)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerStateConvocazione.hour.toString().padStart(2, '0')
                    val m = timePickerStateConvocazione.minute.toString().padStart(2, '0')
                    oraConvocazione = "$h:$m"
                    mostraTimePickerConvocazione = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostraTimePickerConvocazione = false }) { Text("Annulla") }
            }
        )
    }
}

/** Trasforma "sampdoria u13" -> "Sampdoria U13" (prima lettera maiuscola di ogni parola) */
private fun titleCase(input: String): String {
    return input.split(" ").joinToString(" ") { parola ->
        if (parola.isEmpty()) parola
        else parola.substring(0, 1).uppercase() + parola.substring(1)
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
