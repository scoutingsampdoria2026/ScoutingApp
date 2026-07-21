package com.scoutingsampdoria.persone.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.scoutingsampdoria.persone.data.model.LogAdmin
import com.scoutingsampdoria.persone.ui.theme.SampColors
import com.scoutingsampdoria.persone.viewmodel.ConfigViewModel
import com.scoutingsampdoria.persone.viewmodel.FormatoExport
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    personeViewModel: PersoneViewModel,
    onIndietro: () -> Unit,
    onDatiCambiati: () -> Unit
) {
    val context = LocalContext.current
    var nuovoCampo by remember { mutableStateOf("") }
    var mostraDialogSvuota by remember { mutableStateOf(false) }
    var mostraDialogAllinea by remember { mutableStateOf(false) }
    var mostraDialogLog by remember { mutableStateOf<LogAdmin?>(null) }
    var campoDaEliminare by remember { mutableStateOf<Pair<Int, String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.caricaCampi()
        viewModel.caricaLog()
        // Assicuriamoci che i valori disponibili per i filtri export siano popolati
        personeViewModel.caricaLista()
        personeViewModel.caricaCampiCustom()
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val nome = uri.lastPathSegment?.substringAfterLast('/') ?: "import.xlsx"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                viewModel.importaXlsx(nome, bytes) { onDatiCambiati() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurazione", fontWeight = FontWeight.Bold) },
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

            // ------------- Sezione campi personalizzati -------------
            SezioneTitolo("Campi personalizzati", "I campi aggiunti compaiono su tutte le schede. Se non valorizzati, vengono mostrati con \"-\".")

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuovoCampo,
                    onValueChange = { nuovoCampo = it },
                    label = { Text("Nome nuovo campo") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        viewModel.aggiungiCampo(nuovoCampo)
                        nuovoCampo = ""
                    },
                    enabled = !viewModel.caricamento
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Aggiungi campo", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(8.dp))

            viewModel.campi.forEach { campo ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(campo.nome, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { campoDaEliminare = campo.id to campo.nome }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Elimina campo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ------------- Sezione categoria giocatori -------------
            SezioneTitolo(
                "Categoria giocatori",
                "Ricalcola il campo CATEGORIA per tutti i giocatori in base alla loro data di nascita e alla stagione calcistica in corso (dal 1° luglio al 30 giugno)."
            )

            Button(
                onClick = { mostraDialogAllinea = true },
                enabled = !viewModel.caricamento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.SyncAlt, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Allinea categoria con nascita")
            }

            Spacer(Modifier.height(24.dp))

            // ------------- Sezione gestione dati -------------
            SezioneTitolo("Gestione dati", null)

            Button(
                onClick = { filePicker.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                enabled = !viewModel.caricamento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.UploadFile, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Importa da file xlsx")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { mostraDialogSvuota = true },
                enabled = !viewModel.caricamento,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Svuota tutto il database")
            }

            Spacer(Modifier.height(24.dp))

            // ------------- Sezione export -------------
            SezioneTitolo(
                "Esporta dati",
                "Scarica i dati in Excel o PDF. Puoi filtrare cosa esportare senza toccare i filtri della lista principale."
            )

            PannelloFiltriExport(viewModel, personeViewModel)

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.esportaFile(context, FormatoExport.XLSX) { file ->
                            apriFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        }
                    },
                    enabled = !viewModel.caricamento,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.GridOn, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("XLSX")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.esportaFile(context, FormatoExport.PDF) { file ->
                            apriFile(context, file, "application/pdf")
                        }
                    },
                    enabled = !viewModel.caricamento,
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.Rosso),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("PDF")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (viewModel.caricamento) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            viewModel.messaggio?.let {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SampColors.Blu.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(it, color = SampColors.Blu, modifier = Modifier.padding(12.dp))
                }
            }
            viewModel.errore?.let {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ------------- Sezione log -------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Log operazioni", style = MaterialTheme.typography.titleLarge, color = SampColors.Blu, fontWeight = FontWeight.Bold)
                    Text(
                        "Storico modifiche amministrative sul database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = { viewModel.caricaLog() }) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.padding(2.dp))
                    Text("Aggiorna")
                }
            }
            Spacer(Modifier.height(12.dp))

            if (viewModel.logs.isEmpty()) {
                Text(
                    "Nessuna operazione registrata al momento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                viewModel.logs.forEach { log ->
                    LogItem(log = log, onClick = { mostraDialogLog = log })
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ------- Dialog conferma svuotamento -------
    if (mostraDialogSvuota) {
        AlertDialog(
            onDismissRequest = { mostraDialogSvuota = false },
            title = { Text("Svuotare tutto il database?") },
            text = { Text("Verranno eliminati TUTTI i record delle persone. La struttura (campi, utenti) resta intatta. L'operazione non è reversibile.") },
            confirmButton = {
                TextButton(onClick = {
                    mostraDialogSvuota = false
                    viewModel.svuotaDatabase { onDatiCambiati() }
                }) {
                    Text("Sì, svuota", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraDialogSvuota = false }) { Text("Annulla") }
            }
        )
    }

    // ------- Dialog conferma allineamento -------
    if (mostraDialogAllinea) {
        AlertDialog(
            onDismissRequest = { mostraDialogAllinea = false },
            title = { Text("Allineare le categorie?") },
            text = { Text("Il campo CATEGORIA verrà ricalcolato per tutti i giocatori con data di nascita valida, in base alla stagione calcistica corrente. Le modifiche verranno registrate nel log.") },
            confirmButton = {
                TextButton(onClick = {
                    mostraDialogAllinea = false
                    viewModel.allineaCategorie { onDatiCambiati() }
                }) {
                    Text("Sì, allinea", color = SampColors.Blu)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraDialogAllinea = false }) { Text("Annulla") }
            }
        )
    }

    // ------- Dialog eliminazione campo -------
    campoDaEliminare?.let { (id, nome) ->
        AlertDialog(
            onDismissRequest = { campoDaEliminare = null },
            title = { Text("Eliminare il campo \"$nome\"?") },
            text = { Text("Il campo e tutti i suoi valori verranno rimossi da tutte le schede.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminaCampo(id)
                    campoDaEliminare = null
                }) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { campoDaEliminare = null }) { Text("Annulla") }
            }
        )
    }

    // ------- Dialog dettaglio log -------
    mostraDialogLog?.let { log ->
        AlertDialog(
            onDismissRequest = { mostraDialogLog = null },
            title = { Text(descriviTipoLog(log.tipo)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    log.creatoIl?.let {
                        Text("Data: $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    log.utente?.let {
                        Text("Utente: $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(log.dettaglio ?: "(nessun dettaglio)", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostraDialogLog = null }) { Text("Chiudi") }
            }
        )
    }
}

@Composable
private fun SezioneTitolo(titolo: String, descrizione: String?) {
    Text(titolo, style = MaterialTheme.typography.titleLarge, color = SampColors.Blu, fontWeight = FontWeight.Bold)
    descrizione?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun PannelloFiltriExport(
    configViewModel: ConfigViewModel,
    personeViewModel: PersoneViewModel
) {
    val nFiltriAttivi = listOfNotNull(
        configViewModel.exportFiltroRegione,
        configViewModel.exportFiltroRuolo,
        configViewModel.exportFiltroSocieta,
        configViewModel.exportFiltroQuickReport
    ).size + configViewModel.exportFiltriExtra.size

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (nFiltriAttivi > 0) "Filtri export ($nFiltriAttivi attivi)" else "Filtri export (nessuno)",
                    fontWeight = FontWeight.SemiBold,
                    color = SampColors.Blu
                )
                if (nFiltriAttivi > 0) {
                    Text(
                        "Azzera",
                        style = MaterialTheme.typography.labelMedium,
                        color = SampColors.Rosso,
                        modifier = Modifier.clickable { configViewModel.azzeraExportFiltri() }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            FiltroExportMenu("Regione", configViewModel.exportFiltroRegione, personeViewModel.valoriRegione) {
                configViewModel.impostaExportFiltro(regione = it)
            }
            FiltroExportMenu("Ruolo", configViewModel.exportFiltroRuolo, personeViewModel.valoriRuolo) {
                configViewModel.impostaExportFiltro(ruolo = it)
            }
            FiltroExportMenu("Società", configViewModel.exportFiltroSocieta, personeViewModel.valoriSocieta) {
                configViewModel.impostaExportFiltro(societa = it)
            }
            FiltroExportMenu("Quick report", configViewModel.exportFiltroQuickReport, personeViewModel.valoriQuickReport) {
                configViewModel.impostaExportFiltro(quickReport = it)
            }
            personeViewModel.campiCustom.forEach { campo ->
                val valori = personeViewModel.valoriExtra[campo.nome].orEmpty()
                if (valori.isNotEmpty()) {
                    FiltroExportMenu(
                        etichetta = campo.nome,
                        valoreSelezionato = configViewModel.exportFiltriExtra[campo.nome],
                        valoriDisponibili = valori
                    ) { configViewModel.impostaExportFiltroExtra(campo.nome, it) }
                }
            }
        }
    }
}

@Composable
private fun FiltroExportMenu(
    etichetta: String,
    valoreSelezionato: String?,
    valoriDisponibili: List<String>,
    onSeleziona: (String?) -> Unit
) {
    if (valoriDisponibili.isEmpty()) return

    var menuAperto by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
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
                        containerColor = SampColors.Blu.copy(alpha = 0.12f),
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
private fun LogItem(log: LogAdmin, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(descriviTipoLog(log.tipo), fontWeight = FontWeight.SemiBold, color = SampColors.Blu)
                log.creatoIl?.let {
                    Text(it.take(16).replace("T", " "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            log.dettaglio?.let { d ->
                val riassunto = d.lines().firstOrNull().orEmpty()
                Text(
                    riassunto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

private fun descriviTipoLog(tipo: String): String = when (tipo) {
    "allinea_categoria" -> "Allineamento categorie"
    "import" -> "Import xlsx"
    "svuota" -> "Svuotamento DB"
    "export_xlsx" -> "Export xlsx"
    "export_pdf" -> "Export pdf"
    else -> tipo
}

private fun apriFile(context: android.content.Context, file: File, mimeType: String) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Apri con..."))
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Impossibile aprire il file: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}
