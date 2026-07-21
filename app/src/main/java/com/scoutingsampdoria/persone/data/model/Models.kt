package com.scoutingsampdoria.persone.data.model

import com.google.gson.annotations.SerializedName

// ---------- Autenticazione ----------

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val username: String,
    val ruolo: String
)

// ---------- Persone ----------

data class Persona(
    val id: Int,
    val cognome: String,
    val nome: String,
    val societa: String?,
    @SerializedName("data_nascita") val dataNascita: String?,
    val regione: String?,
    val ruolo: String?,
    val matricola: String?,
    @SerializedName("quick_report") val quickReport: String?,
    val extra: Map<String, String>? = null,
    @SerializedName("creato_il") val creatoIl: String? = null,
    @SerializedName("aggiornato_il") val aggiornatoIl: String? = null
)

data class CampoCustom(
    val id: Int,
    val nome: String
)

data class ListaPersoneResponse(
    val totale: Int,
    val pagina: Int,
    @SerializedName("per_pagina") val perPagina: Int,
    val risultati: List<Persona>
)

data class PersonaRequest(
    val cognome: String,
    val nome: String,
    val societa: String?,
    @SerializedName("data_nascita") val dataNascita: String?,
    val regione: String?,
    val ruolo: String?,
    val matricola: String?,
    @SerializedName("quick_report") val quickReport: String?,
    val extra: Map<String, String>? = null
)

data class MessaggioResponse(
    val id: Int? = null,
    val messaggio: String? = null,
    val errore: String? = null
)

data class ImportResponse(
    val messaggio: String?,
    val inserite: Int?,
    val problemi: List<ProblemaImport>?
)

data class ProblemaImport(
    val riga: Int,
    val problema: String
)

data class ConfermaSvuota(
    val conferma: String = "SVUOTA"
)
