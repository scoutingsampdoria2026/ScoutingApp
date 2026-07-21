package com.scoutingsampdoria.persone.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scoutingsampdoria.persone.data.TokenManager
import com.scoutingsampdoria.persone.repository.PersoneRepository

/**
 * Factory semplice, senza dependency injection esterna (Hilt/Koin), per tenere
 * il progetto leggero da comprendere in questa prima versione.
 */
class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val repository = PersoneRepository()
    private val tokenManager = TokenManager(context.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository, tokenManager) as T
            modelClass.isAssignableFrom(PersoneViewModel::class.java) ->
                PersoneViewModel(repository, tokenManager) as T
            modelClass.isAssignableFrom(ConfigViewModel::class.java) ->
                ConfigViewModel(repository, tokenManager) as T
            else -> throw IllegalArgumentException("ViewModel sconosciuto: ${modelClass.name}")
        }
    }
}
