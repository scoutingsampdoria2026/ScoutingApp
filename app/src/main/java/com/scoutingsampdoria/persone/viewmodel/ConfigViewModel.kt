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
