package com.scoutingsampdoria.persone.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

/**
 * Salva e recupera il token JWT, lo username e il ruolo dell'utente loggato.
 * Il token scade dopo 12 ore (vedi TOKEN_EXPIRE_HOURS nel backend Flask);
 * se le chiamate iniziano a fallire con 401, va rifatto il login.
 */
class TokenManager(private val context: Context) {

    private val keyToken = stringPreferencesKey("jwt_token")
    private val keyUsername = stringPreferencesKey("username")
    private val keyRuolo = stringPreferencesKey("ruolo")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[keyToken] }
    val ruoloFlow: Flow<String?> = context.dataStore.data.map { it[keyRuolo] }
    val usernameFlow: Flow<String?> = context.dataStore.data.map { it[keyUsername] }

    suspend fun salvaSessione(token: String, username: String, ruolo: String) {
        context.dataStore.edit { prefs ->
            prefs[keyToken] = token
            prefs[keyUsername] = username
            prefs[keyRuolo] = ruolo
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[keyToken]

    suspend fun getRuolo(): String? = context.dataStore.data.first()[keyRuolo]

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}
