package com.scoutingsampdoria.persone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.repository.ApiResult
import com.scoutingsampdoria.persone.repository.PersoneRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: PersoneRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var loggato by mutableStateOf(false)
        private set
    var ruolo by mutableStateOf<String?>(null)
        private set

    fun login(username: String, password: String, onSuccesso: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            errore = "Inserisci username e password"
            return
        }
        caricamento = true
        errore = null
        viewModelScope.launch {
            when (val risultato = repository.login(username, password)) {
                is ApiResult.Successo -> {
                    tokenManager.salvaSessione(
                        token = risultato.dati.token,
                        username = risultato.dati.username,
                        ruolo = risultato.dati.ruolo
                    )
                    ruolo = risultato.dati.ruolo
                    loggato = true
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

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            tokenManager.logout()
            loggato = false
            ruolo = null
            onLogout()
        }
    }

    /** Da chiamare all'avvio dell'app per capire se c'è già una sessione salvata. */
    fun ripristinaSessione(onEsito: (haSessione: Boolean) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            ruolo = tokenManager.getRuolo()
            loggato = token != null
            onEsito(loggato)
        }
    }
}
