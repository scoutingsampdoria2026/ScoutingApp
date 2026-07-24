package com.scoutingsampdoria.persone.repository

import com.scoutingsampdoria.persone.data.model.AllineaResponse
import com.scoutingsampdoria.persone.data.model.AnteprimaExport
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

    suspend fun exportAnteprima(
        token: String,
        query: String? = null,
        regione: String? = null,
        societa: String? = null,
        ruolo: String? = null,
        quickReport: String? = null,
        filtriExtra: Map<String, String> = emptyMap()
    ): ApiResult<AnteprimaExport> {
        return try {
            val extraQuery = filtriExtra.mapKeys { "extra_${it.key}" }
            gestisciRisposta(
                api.exportAnteprima(
                    ApiClient.bearer(token),
                    query?.takeIf { it.isNotBlank() },
                    regione?.takeIf { it.isNotBlank() },
                    societa?.takeIf { it.isNotBlank() },
                    ruolo?.takeIf { it.isNotBlank() },
                    quickReport?.takeIf { it.isNotBlank() },
                    extraQuery
                )
            )
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

    // ---- Convocazioni ----
    suspend fun listaConvocazioni(token: String, categoria: String? = null): ApiResult<List<com.scoutingsampdoria.persone.data.model.Convocazione>> {
        return try {
            gestisciRisposta(api.listaConvocazioni(ApiClient.bearer(token), categoria))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun dettaglioConvocazione(token: String, id: Int): ApiResult<com.scoutingsampdoria.persone.data.model.Convocazione> {
        return try {
            gestisciRisposta(api.dettaglioConvocazione(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun creaConvocazione(token: String, dati: com.scoutingsampdoria.persone.data.model.ConvocazioneCreaRequest): ApiResult<com.scoutingsampdoria.persone.data.model.MessaggioResponse> {
        return try {
            gestisciRisposta(api.creaConvocazione(ApiClient.bearer(token), dati))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun aggiornaConvocazione(token: String, id: Int, dati: com.scoutingsampdoria.persone.data.model.ConvocazioneAggiornaRequest): ApiResult<com.scoutingsampdoria.persone.data.model.MessaggioResponse> {
        return try {
            gestisciRisposta(api.aggiornaConvocazione(ApiClient.bearer(token), id, dati))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun eliminaConvocazione(token: String, id: Int): ApiResult<com.scoutingsampdoria.persone.data.model.MessaggioResponse> {
        return try {
            gestisciRisposta(api.eliminaConvocazione(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun aggiornaGiocatoriConvocazione(token: String, id: Int, giocatori: List<com.scoutingsampdoria.persone.data.model.ConvocazioneGiocatore>): ApiResult<com.scoutingsampdoria.persone.data.model.MessaggioResponse> {
        return try {
            gestisciRisposta(api.aggiornaGiocatoriConvocazione(
                ApiClient.bearer(token), id,
                com.scoutingsampdoria.persone.data.model.ConvocazioneGiocatoriRequest(giocatori)
            ))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun exportConvocazionePdf(token: String, id: Int, includeCampo: Boolean = true): ApiResult<ByteArray> {
        return try {
            val risposta = api.exportConvocazionePdf(ApiClient.bearer(token), id, if (includeCampo) 1 else 0)
            if (risposta.isSuccessful) {
                val bytes = risposta.body()?.bytes() ?: ByteArray(0)
                ApiResult.Successo(bytes)
            } else {
                ApiResult.Errore("Errore ${risposta.code()}", risposta.code())
            }
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    // ------------------- PROVINI -------------------

    suspend fun listaProviniPersona(token: String, personaId: Int):
            ApiResult<List<com.scoutingsampdoria.persone.data.model.Provino>> {
        return try {
            gestisciRisposta(api.listaProviniPersona(ApiClient.bearer(token), personaId))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun dettaglioProvino(token: String, id: Int):
            ApiResult<com.scoutingsampdoria.persone.data.model.Provino> {
        return try {
            gestisciRisposta(api.dettaglioProvino(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun aggiornaProvino(
        token: String, id: Int,
        req: com.scoutingsampdoria.persone.data.model.ProvinoAggiornaRequest
    ): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.aggiornaProvino(ApiClient.bearer(token), id, req))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun eliminaProvino(token: String, id: Int): ApiResult<MessaggioResponse> {
        return try {
            gestisciRisposta(api.eliminaProvino(ApiClient.bearer(token), id))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun statisticheProvini(token: String):
            ApiResult<com.scoutingsampdoria.persone.data.model.StatisticheProvini> {
        return try {
            gestisciRisposta(api.statisticheProvini(ApiClient.bearer(token)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun elencoProviniGlobale(
        token: String,
        categoria: String? = null,
        data: String? = null,
        soloCompilati: Boolean = false
    ): ApiResult<List<com.scoutingsampdoria.persone.data.model.Provino>> {
        return try {
            gestisciRisposta(api.elencoProviniGlobale(
                ApiClient.bearer(token),
                categoria = categoria?.takeIf { it.isNotBlank() },
                data = data?.takeIf { it.isNotBlank() },
                soloCompilati = if (soloCompilati) 1 else null
            ))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun categorieProvini(token: String): ApiResult<List<String>> {
        return try {
            gestisciRisposta(api.categorieProvini(ApiClient.bearer(token)))
        } catch (e: Exception) {
            ApiResult.Errore("Impossibile contattare il server: ${e.message}")
        }
    }

    suspend fun dateProvini(token: String, categoria: String? = null): ApiResult<List<String>> {
        return try {
            gestisciRisposta(api.dateProvini(
                ApiClient.bearer(token),
                categoria?.takeIf { it.isNotBlank() }
            ))
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
