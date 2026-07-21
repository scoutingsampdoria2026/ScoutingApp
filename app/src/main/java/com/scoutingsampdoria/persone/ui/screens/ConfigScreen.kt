package com.scoutingsampdoria.persone.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    onIndietro: () -> Unit,
    onDatiCambiati: () -> Unit
) {
    val context = LocalContext.current
    var nuovoCampo by remember { mutableStateOf("") }
    var mostraDialogSvuota by remember { mutableStateOf(false) }
    var campoDaEliminare by remember { mutableStateOf<Pair<Int, String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.caricaCampi()
    }

    // File picker per l'xlsx
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
                title = { Text("Configurazione") },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ------------- Sezione campi personalizzati -------------
            Text("Campi personalizzati", style = MaterialTheme.typography.titleLarge)
            Text(
                "I campi aggiunti compaiono su tutte le schede. Se non valorizzati, vengono mostrati con \"-\".",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuovoCampo,
                    onValueChange = { nuovoCampo = it },
                    label = { Text("Nome nuovo campo (es. Piede, Altezza)") },
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
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
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

            // ------------- Sezione dati -------------
            Text("Gestione dati", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(16.dp))

            if (viewModel.caricamento) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            viewModel.messaggio?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
            viewModel.errore?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    // Dialog di conferma svuotamento (doppio passaggio: bottone + conferma)
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
                TextButton(onClick = { mostraDialogSvuota = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog di conferma eliminazione campo
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
                TextButton(onClick = { campoDaEliminare = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}
