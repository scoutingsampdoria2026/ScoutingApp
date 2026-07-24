package com.scoutingsampdoria.persone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.data.model.Provino
import com.scoutingsampdoria.persone.data.model.ProvinoAggiornaRequest
import com.scoutingsampdoria.persone.data.model.StatisticheProvini
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

    // --- Dashboard ---
    var statistiche by mutableStateOf<StatisticheProvini?>(null)
        private set
    var proviniDashboard by mutableStateOf<List<Provino>>(emptyList())
        private set
    var categorieDisponibili by mutableStateOf<List<String>>(emptyList())
        private set
    var dateDisponibili by mutableStateOf<List<String>>(emptyList())
        private set
    var filtroCategoria by mutableStateOf<String?>(null)
        private set
    var filtroData by mutableStateOf<String?>(null)
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

    fun caricaStatistiche() {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.statisticheProvini(token)) {
                is ApiResult.Successo -> statistiche = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
        }
    }

    fun caricaCategorieDashboard() {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.categorieProvini(token)) {
                is ApiResult.Successo -> categorieDisponibili = r.dati
                is ApiResult.Errore -> { /* silente */ }
            }
        }
    }

    fun caricaDateDashboard(categoria: String? = null) {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.dateProvini(token, categoria)) {
                is ApiResult.Successo -> dateDisponibili = r.dati
                is ApiResult.Errore -> { /* silente */ }
            }
        }
    }

    fun caricaDashboard(categoria: String? = filtroCategoria, data: String? = filtroData) {
        filtroCategoria = categoria
        filtroData = data
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.elencoProviniGlobale(
                token = token,
                categoria = categoria,
                data = data,
                soloCompilati = false
            )) {
                is ApiResult.Successo -> proviniDashboard = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun impostaFiltroCategoria(categoria: String?) {
        filtroCategoria = categoria
        // Reset data se il filtro categoria cambia (le date dipendono dalla categoria)
        filtroData = null
        caricaDateDashboard(categoria)
        caricaDashboard()
    }

    fun impostaFiltroData(data: String?) {
        filtroData = data
        caricaDashboard()
    }

    fun resetFiltriDashboard() {
        filtroCategoria = null
        filtroData = null
        caricaDateDashboard(null)
        caricaDashboard()
    }

    fun pulisciMessaggi() {
        errore = null
        messaggio = null
    }
}
