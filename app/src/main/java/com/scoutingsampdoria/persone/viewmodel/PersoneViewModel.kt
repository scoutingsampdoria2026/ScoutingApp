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

    fun caricaCampiCustom() {
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.listaCampi(token)) {
                is ApiResult.Successo -> campiCustom = r.dati
                is ApiResult.Errore -> { /* non blocca la schermata */ }
            }
        }
    }

    fun caricaLista(query: String? = null, regione: String? = null) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                errore = "Sessione scaduta, effettua di nuovo il login"
                caricamento = false
                return@launch
            }
            when (val risultato = repository.listaPersone(token, query = query, regione = regione)) {
                is ApiResult.Successo -> {
                    persone = risultato.dati.risultati
                    totale = risultato.dati.totale
                    caricamento = false
                }
                is ApiResult.Errore -> {
                    errore = risultato.messaggio
                    caricamento = false
                }
            }
        }
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
