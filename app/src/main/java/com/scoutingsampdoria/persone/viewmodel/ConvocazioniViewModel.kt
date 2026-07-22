package com.scoutingsampdoria.persone.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.data.model.Convocazione
import com.scoutingsampdoria.persone.data.model.ConvocazioneAggiornaRequest
import com.scoutingsampdoria.persone.data.model.ConvocazioneCreaRequest
import com.scoutingsampdoria.persone.data.model.ConvocazioneGiocatore
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.repository.ApiResult
import com.scoutingsampdoria.persone.repository.PersoneRepository
import kotlinx.coroutines.launch

class ConvocazioniViewModel(
    private val repository: PersoneRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    // Categorie disponibili (viene popolata dalla PersoneViewModel/lista giocatori)
    var categorieDisponibili by mutableStateOf<List<String>>(emptyList())
        private set

    // Lista convocazioni per la categoria corrente
    var convocazioni by mutableStateOf<List<Convocazione>>(emptyList())
        private set

    // Convocazione attualmente in modifica
    var convocazioneCorrente by mutableStateOf<Convocazione?>(null)
        private set

    // Giocatori disponibili per la categoria (usati nel dropdown convocati)
    var giocatoriCategoria by mutableStateOf<List<Persona>>(emptyList())
        private set

    fun caricaCategorieDaPersone(personeList: List<Persona>) {
        val cats = personeList
            .mapNotNull { it.extra?.get("CATEGORIA")?.takeIf { c -> c.isNotBlank() } }
            .distinct()
            .sorted()
        categorieDisponibili = cats
    }

    fun caricaConvocazioniPerCategoria(categoria: String) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.listaConvocazioni(token, categoria)) {
                is ApiResult.Successo -> convocazioni = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun caricaGiocatoriCategoria(personeList: List<Persona>, categoria: String) {
        giocatoriCategoria = personeList
            .filter { it.extra?.get("CATEGORIA") == categoria }
            .sortedWith(compareBy({ it.cognome }, { it.nome }))
    }

    fun caricaDettaglioConvocazione(id: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.dettaglioConvocazione(token, id)) {
                is ApiResult.Successo -> convocazioneCorrente = r.dati
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun creaConvocazione(
        categoria: String,
        data: String?,
        ora: String?,
        impianto: String?,
        squadraCasa: String?,
        squadraOspite: String?,
        modulo: String?,
        onCreata: (Int) -> Unit
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            val req = ConvocazioneCreaRequest(
                categoria = categoria,
                data = data,
                ora = ora,
                impianto = impianto,
                squadraCasa = squadraCasa,
                squadraOspite = squadraOspite,
                modulo = modulo,
                numeroCaselle = 20
            )
            when (val r = repository.creaConvocazione(token, req)) {
                is ApiResult.Successo -> {
                    messaggio = "Convocazione creata"
                    // Recupero l'id dal messaggio... o meglio ricarico e prendo la più recente
                    caricaConvocazioniPerCategoria(categoria)
                    // L'endpoint restituisce id ma nel MessaggioResponse non c'è, quindi ricarichiamo
                    val listaAggiornata = repository.listaConvocazioni(token, categoria)
                    if (listaAggiornata is ApiResult.Successo) {
                        listaAggiornata.dati.firstOrNull()?.let { onCreata(it.id) }
                    }
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun aggiornaConvocazione(
        id: Int,
        data: String?,
        ora: String?,
        impianto: String?,
        squadraCasa: String?,
        squadraOspite: String?,
        modulo: String?,
        onCompletato: () -> Unit
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            val req = ConvocazioneAggiornaRequest(
                data = data, ora = ora, impianto = impianto,
                squadraCasa = squadraCasa, squadraOspite = squadraOspite, modulo = modulo
            )
            when (val r = repository.aggiornaConvocazione(token, id, req)) {
                is ApiResult.Successo -> {
                    messaggio = "Convocazione salvata"
                    caricaDettaglioConvocazione(id)
                    onCompletato()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun aggiornaGiocatori(
        convocazioneId: Int,
        giocatori: List<ConvocazioneGiocatore>,
        onCompletato: () -> Unit
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.aggiornaGiocatoriConvocazione(token, convocazioneId, giocatori)) {
                is ApiResult.Successo -> {
                    messaggio = "Distinta aggiornata"
                    caricaDettaglioConvocazione(convocazioneId)
                    onCompletato()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun eliminaConvocazione(id: Int, categoria: String, onCompletato: () -> Unit) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.eliminaConvocazione(token, id)) {
                is ApiResult.Successo -> {
                    messaggio = "Convocazione eliminata"
                    caricaConvocazioniPerCategoria(categoria)
                    onCompletato()
                }
                is ApiResult.Errore -> errore = r.messaggio
            }
            caricamento = false
        }
    }

    fun esportaConvocazionePdf(id: Int, onCompletato: (ByteArray) -> Unit) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            val token = tokenManager.getToken() ?: return@launch
            when (val r = repository.exportConvocazionePdf(token, id)) {
                is ApiResult.Successo -> {
                    if (r.dati.isEmpty()) errore = "Il file è vuoto"
                    else {
                        messaggio = "PDF pronto (${r.dati.size / 1024} KB)"
                        onCompletato(r.dati)
                    }
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
