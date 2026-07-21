package com.scoutingsampdoria.persone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.data.model.CampoCustom
import com.scoutingsampdoria.persone.data.model.LogAdmin
import com.scoutingsampdoria.persone.repository.ApiResult
import com.scoutingsampdoria.persone.repository.PersoneRepository
import kotlinx.coroutines.launch

class ConfigViewModel(
    private val repository: PersoneRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set
    var campi by mutableStateOf<List<CampoCustom>>(emptyList())
        private set
    var logs by mutableStateOf<List<LogAdmin>>(emptyList())
        private set

    // Filtri per export (indipendenti da quelli della lista)
    var exportFiltroRegione by mutableStateOf<String?>(null)
        private set
    var exportFiltroRuolo by mutableStateOf<String?>(null)
        private set
    var exportFiltroSocieta by mutableStateOf<String?>(null)
        private set
    var exportFiltroQuickReport by mutableStateOf<String?>(null)
        private set
    var exportFiltriExtra by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    fun impostaExportFiltro(
        regione: String? = exportFiltroRegione,
        ruolo: String? = exportFiltroRuolo,
        societa: String? = exportFiltroSocieta,
        quickReport: String? = exportFiltroQuickReport
    ) {
        exportFiltroRegione = regione
        exportFiltroRuolo = ruolo
        exportFiltroSocieta = societa
        exportFiltroQuickReport = quickReport
    }

    fun impostaExportFiltroExtra(nomeCampo: String, valore: String?) {
        exportFiltriExtra = if (valore == null) {
            exportFiltriExtra - nomeCampo
        } else {
            exportFiltriExtra + (nomeCampo to valore)
        }
    }

    fun azzeraExportFiltri() {
        exportFiltroRegione = null
        exportFiltroRuolo = null
        exportFiltroSocieta = null
        exportFiltroQuickReport = null
        exportFiltriExtra = emptyMap()
    }

    fun caricaCampi() {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.listaCampi(token)) {
                is ApiResult.Successo -> campi = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
        }
    }

    fun caricaLog() {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.listaLog(token)) {
                is ApiResult.Successo -> logs = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
        }
    }

    fun allineaCategorie(onCompletato: () -> Unit) {
        caricamento = true
        errore = null
        messaggio = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.allineaCategorie(token)) {
                is ApiResult.Successo -> {
                    messaggio = r.dati.messaggio ?: "Allineamento completato"
                    caricaLog()
                    onCompletato()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    /**
     * Scarica i bytes del file esportato applicando i filtri correnti e li
     * restituisce tramite callback. Il salvataggio effettivo avviene lato UI
     * tramite Storage Access Framework.
     */
    fun esportaFileInMemoria(formato: FormatoExport, onCompletato: (ByteArray) -> Unit) {
        caricamento = true
        errore = null
        messaggio = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            val risultato = when (formato) {
                FormatoExport.XLSX -> repository.exportXlsx(
                    token,
                    regione = exportFiltroRegione,
                    ruolo = exportFiltroRuolo,
                    societa = exportFiltroSocieta,
                    quickReport = exportFiltroQuickReport,
                    filtriExtra = exportFiltriExtra
                )
                FormatoExport.PDF -> repository.exportPdf(
                    token,
                    regione = exportFiltroRegione,
                    ruolo = exportFiltroRuolo,
                    societa = exportFiltroSocieta,
                    quickReport = exportFiltroQuickReport,
                    filtriExtra = exportFiltriExtra
                )
            }
            when (risultato) {
                is ApiResult.Successo -> {
                    val bytes = risultato.dati
                    if (bytes.isEmpty()) {
                        errore = "Il file scaricato è vuoto"
                    } else {
                        messaggio = "File pronto (${bytes.size / 1024} KB) — scegli dove salvarlo"
                        caricaLog()
                        onCompletato(bytes)
                    }
                }
                is ApiResult.Errore -> errore = risultato.messaggio
            }
            caricamento = false
        }
    }

    fun aggiungiCampo(nome: String) {
        if (nome.isBlank()) {
            errore = "Il nome del campo non può essere vuoto"
            return
        }
        caricamento = true
        errore = null
        messaggio = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.creaCampo(token, nome.trim())) {
                is ApiResult.Successo -> {
                    messaggio = r.dati.messaggio
                    caricaCampi()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun eliminaCampo(id: Int) {
        caricamento = true
        errore = null
        messaggio = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.eliminaCampo(token, id)) {
                is ApiResult.Successo -> {
                    messaggio = r.dati.messaggio
                    caricaCampi()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun svuotaDatabase(onCompletato: () -> Unit) {
        caricamento = true
        errore = null
        messaggio = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.svuotaDatabase(token)) {
                is ApiResult.Successo -> {
                    messaggio = r.dati.messaggio
                    onCompletato()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun importaXlsx(nomeFile: String, bytes: ByteArray, onCompletato: () -> Unit) {
        caricamento = true
        errore = null
        messaggio = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.importaXlsx(token, nomeFile, bytes)) {
                is ApiResult.Successo -> {
                    val problemi = r.dati.problemi.orEmpty()
                    messaggio = buildString {
                        append(r.dati.messaggio ?: "Import completato")
                        if (problemi.isNotEmpty()) {
                            append("\nRighe con problemi: ")
                            append(problemi.joinToString("; ") { "riga ${it.riga}: ${it.problema}" })
                        }
                    }
                    onCompletato()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun pulisciMessaggi() {
        errore = null
        messaggio = null
    }
}

enum class FormatoExport { XLSX, PDF }
