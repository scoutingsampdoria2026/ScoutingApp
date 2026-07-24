package com.scoutingsampdoria.persone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.data.model.Provino
import com.scoutingsampdoria.persone.data.model.ProvinoAggiornaRequest
import com.scoutingsampdoria.persone.repository.ApiResult
import com.scoutingsampdoria.persone.repository.PersoneRepository
import kotlinx.coroutines.launch

class ProviniViewModel(
    private val repository: PersoneRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    var provini by mutableStateOf<List<Provino>>(emptyList())
        private set

    var provinoCorrente by mutableStateOf<Provino?>(null)
        private set

    fun caricaProviniPersona(personaId: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.listaProviniPersona(token, personaId)) {
                is ApiResult.Successo -> provini = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun caricaDettaglio(provinoId: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.dettaglioProvino(token, provinoId)) {
                is ApiResult.Successo -> provinoCorrente = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun aggiornaProvino(
        provinoId: Int,
        presenza: String?,
        giudizio: Int?,
        note: String?,
        onCompletato: () -> Unit
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            val req = ProvinoAggiornaRequest(presenza = presenza, giudizio = giudizio, note = note)
            when (val r = repository.aggiornaProvino(token, provinoId, req)) {
                is ApiResult.Successo -> {
                    messaggio = "Provino aggiornato"
                    caricaDettaglio(provinoId)
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
