package com.scoutingsampdoria.persone.repository

import com.scoutingsampdoria.persone.data.model.AllineaResponse
import com.scoutingsampdoria.persone.data.model.CampoCustom
import com.scoutingsampdoria.persone.data.model.ImportResponse
import com.scoutingsampdoria.persone.data.model.ListaPersoneResponse
import com.scoutingsampdoria.persone.data.model.LogAdmin
import com.scoutingsampdoria.persone.data.model.LoginRequest
import com.scoutingsampdoria.persone.data.model.LoginResponse
import com.scoutingsampdoria.persone.data.model.MessaggioResponse
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.data.model.PersonaRequest
import com.scoutingsampdoria.persone.data.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

/** Risultato semplice per non far leggere Retrofit al resto dell'app. */
sealed class ApiResult<out T> {
    data class Successo<T>(val dati: T) : ApiResult<T>()
    data class Errore(val messaggio: String, val codice: Int? = null) : ApiResult<Nothing>()
}

class PersoneRepository {

    private val api = ApiClient.api

    suspend fun login(username: String, password: String): ApiResult<LoginResponse> {
        return try {
            val risposta = api.login(LoginRequest(username, password))
            gestisciRisposta(risposta)
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun listaPersone(
        token: String,
        query: String? = null,
        regione: String? = null,
        societa: String? = null,
        ruolo: String? = null,
        page: Int = 1
    ): ApiResult<ListaPersoneResponse> {
        return try {
            val risposta = api.listaPersone(
                token = ApiClient.bearer(token),
                query = query?.takeIf { it.isNotBlank() },
                regione = regione?.takeIf { it.isNotBlank() },
                societa = societa?.takeIf { it.isNotBlank() },
                ruolo = ruolo?.takeIf { it.isNotBlank() },
                page = page
            )
            gestisciRisposta(risposta)
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun getPersona(token: String, id: Int): ApiResult<Persona> {
        return try {
            gestisciRisposta(api.getPersona(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun creaPersona(token: String, persona: PersonaRequest): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.creaPersona(ApiClient.bearer(token), persona))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun modificaPersona(token: String, id: Int, persona: PersonaRequest): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.modificaPersona(ApiClient.bearer(token), id, persona))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun eliminaPersona(token: String, id: Int): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.eliminaPersona(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun listaCampi(token: String): ApiResult<List<CampoCustom>> {
        return try {
            gestisciRisposta(api.listaCampi(ApiClient.bearer(token)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun creaCampo(token: String, nome: String): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.creaCampo(ApiClient.bearer(token), mapOf("nome" to nome)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun eliminaCampo(token: String, id: Int): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.eliminaCampo(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun svuotaDatabase(token: String): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.svuotaDatabase(ApiClient.bearer(token)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun importaXlsx(token: String, nomeFile: String, bytes: ByteArray): ApiResult<ImportResponse> {
        return try {
            val requestBody = bytes.toRequestBody(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull()
            )
            val part = MultipartBody.Part.createFormData("file", nomeFile, requestBody)
            gestisciRisposta(api.importaXlsx(ApiClient.bearer(token), part))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun allineaCategorie(token: String): ApiResult<AllineaResponse> {
        return try {
            gestisciRisposta(api.allineaCategorie(ApiClient.bearer(token)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun listaLog(token: String): ApiResult<List<LogAdmin>> {
        return try {
            gestisciRisposta(api.listaLog(ApiClient.bearer(token)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    /** Scarica un export xlsx applicando i filtri opzionali. Ritorna i bytes del file. */
    suspend fun exportXlsx(
        token: String,
        query: String? = null,
        regione: String? = null,
        societa: String? = null,
        ruolo: String? = null,
        quickReport: String? = null,
        filtriExtra: Map<String, String> = emptyMap()
    ): ApiResult<ByteArray> {
        return try {
            val extraQuery = filtriExtra.mapKeys { "extra_${it.key}" }
            val risposta = api.exportXlsx(
                ApiClient.bearer(token),
                query?.takeIf { it.isNotBlank() },
                regione?.takeIf { it.isNotBlank() },
                societa?.takeIf { it.isNotBlank() },
                ruolo?.takeIf { it.isNotBlank() },
                quickReport?.takeIf { it.isNotBlank() },
                extraQuery
            )
            if (risposta.isSuccessful) {
                val bytes = risposta.body()?.bytes() ?: ByteArray(0)
                ApiResult.Successo(bytes)
            } else {
                ApiResult.Errore("Errore ${risposta.code()} durante l'export", risposta.code())
            }
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun exportPdf(
        token: String,
        query: String? = null,
        regione: String? = null,
        societa: String? = null,
        ruolo: String? = null,
        quickReport: String? = null,
        filtriExtra: Map<String, String> = emptyMap()
    ): ApiResult<ByteArray> {
        return try {
            val extraQuery = filtriExtra.mapKeys { "extra_${it.key}" }
            val risposta = api.exportPdf(
                ApiClient.bearer(token),
                query?.takeIf { it.isNotBlank() },
                regione?.takeIf { it.isNotBlank() },
                societa?.takeIf { it.isNotBlank() },
                ruolo?.takeIf { it.isNotBlank() },
                quickReport?.takeIf { it.isNotBlank() },
                extraQuery
            )
            if (risposta.isSuccessful) {
                val bytes = risposta.body()?.bytes() ?: ByteArray(0)
                ApiResult.Successo(bytes)
            } else {
                ApiResult.Errore("Errore ${risposta.code()} durante l'export", risposta.code())
            }
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    private fun <T> gestisciRisposta(risposta: Response<T>): ApiResult<T> {
        return if (risposta.isSuccessful && risposta.body() != null) {
            ApiResult.Successo(risposta.body()!!)
        } else {
            val corpoErrore = risposta.errorBody()?.string()
            val messaggio = corpoErrore
                ?.substringAfter("\"errore\":\"")
                ?.substringBefore("\"")
                ?.takeIf { it.isNotBlank() }
                ?: "Errore ${risposta.code()}"
            ApiResult.Errore(messaggio, risposta.code())
        }
    }
}
