package com.scoutingsampdoria.persone.data.network

import com.scoutingsampdoria.persone.data.model.AllineaResponse
import com.scoutingsampdoria.persone.data.model.CampoCustom
import com.scoutingsampdoria.persone.data.model.ConfermaSvuota
import com.scoutingsampdoria.persone.data.model.ImportResponse
import com.scoutingsampdoria.persone.data.model.ListaPersoneResponse
import com.scoutingsampdoria.persone.data.model.LogAdmin
import com.scoutingsampdoria.persone.data.model.LoginRequest
import com.scoutingsampdoria.persone.data.model.LoginResponse
import com.scoutingsampdoria.persone.data.model.MessaggioResponse
import com.scoutingsampdoria.persone.data.model.Persona
import com.scoutingsampdoria.persone.data.model.PersonaRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface PersoneApi {

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/persone")
    suspend fun listaPersone(
        @Header("Authorization") token: String,
        @Query("q") query: String? = null,
        @Query("regione") regione: String? = null,
        @Query("societa") societa: String? = null,
        @Query("ruolo") ruolo: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): Response<ListaPersoneResponse>

    @GET("api/persone/{id}")
    suspend fun getPersona(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Persona>

    @POST("api/persone")
    suspend fun creaPersona(
        @Header("Authorization") token: String,
        @Body persona: PersonaRequest
    ): Response<MessaggioResponse>

    @PUT("api/persone/{id}")
    suspend fun modificaPersona(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body persona: PersonaRequest
    ): Response<MessaggioResponse>

    @DELETE("api/persone/{id}")
    suspend fun eliminaPersona(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessaggioResponse>

    // ---------- Campi personalizzati ----------

    @GET("api/campi")
    suspend fun listaCampi(
        @Header("Authorization") token: String
    ): Response<List<CampoCustom>>

    @POST("api/campi")
    suspend fun creaCampo(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<MessaggioResponse>

    @DELETE("api/campi/{id}")
    suspend fun eliminaCampo(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessaggioResponse>

    // ---------- Amministrazione ----------

    @POST("api/admin/svuota")
    suspend fun svuotaDatabase(
        @Header("Authorization") token: String,
        @Body body: ConfermaSvuota = ConfermaSvuota()
    ): Response<MessaggioResponse>

    @Multipart
    @POST("api/admin/importa")
    suspend fun importaXlsx(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ImportResponse>

    @POST("api/admin/allinea-categorie")
    suspend fun allineaCategorie(
        @Header("Authorization") token: String
    ): Response<AllineaResponse>

    @GET("api/admin/log")
    suspend fun listaLog(
        @Header("Authorization") token: String,
        @Query("limite") limite: Int = 50
    ): Response<List<LogAdmin>>

    @GET("api/admin/export-xlsx")
    suspend fun exportXlsx(
        @Header("Authorization") token: String,
        @Query("q") query: String? = null,
        @Query("regione") regione: String? = null,
        @Query("societa") societa: String? = null,
        @Query("ruolo") ruolo: String? = null,
        @Query("quick_report") quickReport: String? = null,
        @QueryMap extra: Map<String, String> = emptyMap()
    ): Response<okhttp3.ResponseBody>

    @GET("api/admin/export-pdf")
    suspend fun exportPdf(
        @Header("Authorization") token: String,
        @Query("q") query: String? = null,
        @Query("regione") regione: String? = null,
        @Query("societa") societa: String? = null,
        @Query("ruolo") ruolo: String? = null,
        @Query("quick_report") quickReport: String? = null,
        @QueryMap extra: Map<String, String> = emptyMap()
    ): Response<okhttp3.ResponseBody>
}
