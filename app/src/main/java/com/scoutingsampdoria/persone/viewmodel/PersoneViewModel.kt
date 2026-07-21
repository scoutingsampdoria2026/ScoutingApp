package com.scoutingsampdoria.persone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.data.model.CampoCustom
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.data.model.PersonaRequest
import com.scoutingsampdoria.persone.repository.ApiResult
import com.scoutingsampdoria.persone.repository.PersoneRepository
import kotlinx.coroutines.launch

class PersoneViewModel(
    private val repository: PersoneRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var persone by mutableStateOf<List<Persona>>(emptyList())
        private set
    var totale by mutableStateOf(0)
        private set
    var personaSelezionata by mutableStateOf<Persona?>(null)
        private set
    var messaggioSuccesso by mutableStateOf<String?>(null)
        private set
    var campiCustom by mutableStateOf<List<CampoCustom>>(emptyList())
        private set

    // Filtri correnti (per riapplicarli al ricarico)
    var filtroQuery by mutableStateOf<String?>(null)
        private set
    var filtroRegione by mutableStateOf<String?>(null)
        private set
    var filtroRuolo by mutableStateOf<String?>(null)
        private set
    var filtroSocieta by mutableStateOf<String?>(null)
        private set
    var filtroQuickReport by mutableStateOf<String?>(null)
        private set
    // Filtri per campi custom: nome campo -> valore selezionato
    var filtriExtra by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    // Valori distinti disponibili per i menu a tendina
    var valoriRegione by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriRuolo by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriSocieta by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriQuickReport by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriExtra by mutableStateOf<Map<String, List<String>>>(emptyMap())
        private set

    fun caricaCampiCustom() {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.listaCampi(token)) {
                is ApiResult.Successo -> campiCustom = r.dati
                is ApiResult.Errore -> { /* non blocca la schermata */ }
            }
        }
    }

    fun impostaFiltro(query: String? = filtroQuery, regione: String? = filtroRegione, ruolo: String? = filtroRuolo,
                     societa: String? = filtroSocieta, quickReport: String? = filtroQuickReport) {
        filtroQuery = query
        filtroRegione = regione
        filtroRuolo = ruolo
        filtroSocieta = societa
        filtroQuickReport = quickReport
        caricaLista()
    }

    fun impostaFiltroExtra(nomeCampo: String, valore: String?) {
        filtriExtra = if (valore == null) {
            filtriExtra - nomeCampo
        } else {
            filtriExtra + (nomeCampo to valore)
        }
        caricaLista()
    }

    fun azzeraFiltri() {
        filtroQuery = null
        filtroRegione = null
        filtroRuolo = null
        filtroSocieta = null
        filtroQuickReport = null
        filtriExtra = emptyMap()
        caricaLista()
    }

    fun caricaLista(query: String? = filtroQuery) {
        if (query != filtroQuery) filtroQuery = query
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                errore = "Sessione scaduta, effettua di nuovo il login"
                caricamento = false
                return@launch
            }
            when (val risultato = repository.listaPersone(
                token,
                query = filtroQuery,
                regione = filtroRegione,
                societa = filtroSocieta,
                ruolo = filtroRuolo
            )) {
                is ApiResult.Successo -> {
                    var lista = risultato.dati.risultati

                    // Filtro locale per quick_report (l'API già lo supporta ma per consistenza)
                    filtroQuickReport?.let { qr ->
                        lista = lista.filter { it.quickReport == qr }
                    }

                    // Filtri locali sui campi custom (extra)
                    if (filtriExtra.isNotEmpty()) {
                        lista = lista.filter { p ->
                            filtriExtra.all { (campo, valore) ->
                                p.extra?.get(campo) == valore
                            }
                        }
                    }

                    persone = lista
                    totale = risultato.dati.totale
                    // Aggiorna i valori disponibili dai risultati (per popolare i menu)
                    aggiornaValoriDisponibili(risultato.dati.risultati)
                    caricamento = false
                }
                is ApiResult.Errore -> {
                    errore = risultato.messaggio
                    caricamento = false
                }
            }
        }
    }

    private fun aggiornaValoriDisponibili(tutte: List<Persona>) {
        valoriRegione = tutte.mapNotNull { it.regione?.takeIf { s -> s.isNotBlank() } }.distinct().sorted()
        valoriRuolo = tutte.mapNotNull { it.ruolo?.takeIf { s -> s.isNotBlank() } }.distinct().sorted()
        valoriSocieta = tutte.mapNotNull { it.societa?.takeIf { s -> s.isNotBlank() } }.distinct().sorted()
        valoriQuickReport = tutte.mapNotNull { it.quickReport?.takeIf { s -> s.isNotBlank() } }.distinct().sorted()

        // Per ogni campo custom, raccogli i valori distinti presenti nei dati
        val valoriPerCampo = mutableMapOf<String, List<String>>()
        campiCustom.forEach { campo ->
            val valori = tutte.mapNotNull { it.extra?.get(campo.nome)?.takeIf { s -> s.isNotBlank() } }
                .distinct().sorted()
            if (valori.isNotEmpty()) valoriPerCampo[campo.nome] = valori
        }
        valoriExtra = valoriPerCampo
    }

    fun caricaDettaglio(id: Int) {
        caricamento = true
        errore = null
        personaSelezionata = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val risultato = repository.getPersona(token, id)) {
                is ApiResult.Successo -> {
                    personaSelezionata = risultato.dati
                    caricamento = false
                }
                is ApiResult.Errore -> {
                    errore = risultato.messaggio
                    caricamento = false
                }
            }
        }
    }

    fun salvaPersona(id: Int?, persona: PersonaRequest, onSuccesso: () -> Unit) {
        caricamento = true
        errore = null
        messaggioSuccesso = null
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                errore = "Sessione scaduta, effettua di nuovo il login"
                caricamento = false
                return@launch
            }
            val risultato = if (id == null) {
                repository.creaPersona(token, persona)
            } else {
                repository.modificaPersona(token, id, persona)
            }
            when (risultato) {
                is ApiResult.Successo -> {
                    messaggioSuccesso = risultato.dati.messaggio ?: "Salvato con successo"
                    caricamento = false
                    onSuccesso()
                }
                is ApiResult.Errore -> {
                    errore = risultato.messaggio
                    caricamento = false
                }
            }
        }
    }

    fun eliminaPersona(id: Int, onSuccesso: () -> Unit) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val risultato = repository.eliminaPersona(token, id)) {
                is ApiResult.Successo -> {
                    caricamento = false
                    onSuccesso()
                }
                is ApiResult.Errore -> {
                    errore = risultato.messaggio
                    caricamento = false
                }
            }
        }
    }

    fun pulisciErrore() {
        errore = null
    }
}
