package com.scoutingsampdoria.persone.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scoutingsampdoria.persone.ui.screens.ConfigScreen
import com.scoutingsampdoria.persone.ui.screens.ConvocazioneDetailScreen
import com.scoutingsampdoria.persone.ui.screens.ConvocazioniHomeScreen
import com.scoutingsampdoria.persone.ui.screens.ConvocazioniListaScreen
import com.scoutingsampdoria.persone.ui.screens.ElencoProviniScreen
import com.scoutingsampdoria.persone.ui.screens.HomeScreen
import com.scoutingsampdoria.persone.ui.screens.LoginScreen
import com.scoutingsampdoria.persone.ui.screens.PersonDetailScreen
import com.scoutingsampdoria.persone.ui.screens.PersonFormScreen
import com.scoutingsampdoria.persone.ui.screens.PersonListScreen
import com.scoutingsampdoria.persone.ui.screens.ProvinoDetailScreen
import com.scoutingsampdoria.persone.util.MonitorInattivita
import com.scoutingsampdoria.persone.util.tracciaInterazione
import com.scoutingsampdoria.persone.viewmodel.AuthViewModel
import com.scoutingsampdoria.persone.viewmodel.ConfigViewModel
import com.scoutingsampdoria.persone.viewmodel.ConvocazioniViewModel
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel
import com.scoutingsampdoria.persone.viewmodel.ProviniViewModel
import com.scoutingsampdoria.persone.viewmodel.ViewModelFactory

private object Rotte {
    const val LOGIN = "login"
    const val HOME = "home"
    const val LISTA = "lista"
    const val DETTAGLIO = "dettaglio/{personaId}"
    const val NUOVA = "nuova"
    const val MODIFICA = "modifica/{personaId}"
    const val CONFIG = "config"
    const val CONVOCAZIONI_HOME = "convocazioni_home"
    const val CONVOCAZIONI_LISTA = "convocazioni_lista/{categoria}"
    const val CONVOCAZIONE_DETTAGLIO = "convocazione/{convocazioneId}/{categoria}"
    const val ELENCO_PROVINI = "provini/{personaId}"
    const val DETTAGLIO_PROVINO = "provino/{provinoId}"

    fun dettaglio(id: Int) = "dettaglio/$id"
    fun modifica(id: Int) = "modifica/$id"
    fun convocazioniLista(categoria: String) = "convocazioni_lista/${java.net.URLEncoder.encode(categoria, "UTF-8")}"
    fun convocazioneDettaglio(id: Int, categoria: String) = "convocazione/$id/${java.net.URLEncoder.encode(categoria, "UTF-8")}"
    fun elencoProvini(personaId: Int) = "provini/$personaId"
    fun dettaglioProvino(provinoId: Int) = "provino/$provinoId"
}

@Composable
fun ScoutingNavGraph(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val personeViewModel: PersoneViewModel = viewModel(factory = factory)
    val configViewModel: ConfigViewModel = viewModel(factory = factory)
    val convocazioniViewModel: ConvocazioniViewModel = viewModel(factory = factory)
    val proviniViewModel: ProviniViewModel = viewModel(factory = factory)

    var sessioneVerificata by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.ripristinaSessione { haSessione ->
            sessioneVerificata = true
        }
    }

    if (!sessioneVerificata) return

    // Tracker inattività: si aggiorna a ogni tocco. Se supera 30 min → logoff automatico.
    var ultimaInterazione by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Il monitor gira solo quando l'utente è loggato
    if (authViewModel.loggato) {
        MonitorInattivita(ultimaInterazione) {
            authViewModel.logout {
                navController.navigate(Rotte.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .tracciaInterazione { ultimaInterazione = System.currentTimeMillis() }
    ) {
        NavHost(
            navController = navController,
            startDestination = if (authViewModel.loggato) Rotte.HOME else Rotte.LOGIN
        ) {
        composable(Rotte.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccesso = {
                    navController.navigate(Rotte.HOME) {
                        popUpTo(Rotte.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotte.HOME) {
            HomeScreen(
                ruoloUtente = authViewModel.ruolo,
                onGestioneGiocatori = { navController.navigate(Rotte.LISTA) },
                onConvocazioni = { navController.navigate(Rotte.CONVOCAZIONI_HOME) },
                onConfigurazione = { navController.navigate(Rotte.CONFIG) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Rotte.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Rotte.CONVOCAZIONI_HOME) {
            ConvocazioniHomeScreen(
                personeViewModel = personeViewModel,
                convocazioniViewModel = convocazioniViewModel,
                onIndietro = { navController.popBackStack() },
                onCategoriaSelezionata = { cat -> navController.navigate(Rotte.convocazioniLista(cat)) }
            )
        }

        composable(
            route = Rotte.CONVOCAZIONI_LISTA,
            arguments = listOf(navArgument("categoria") { type = NavType.StringType })
        ) { backStackEntry ->
            val cat = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("categoria") ?: "", "UTF-8"
            )
            ConvocazioniListaScreen(
                categoria = cat,
                viewModel = convocazioniViewModel,
                onIndietro = { navController.popBackStack() },
                onApriConvocazione = { id -> navController.navigate(Rotte.convocazioneDettaglio(id, cat)) }
            )
        }

        composable(
            route = Rotte.CONVOCAZIONE_DETTAGLIO,
            arguments = listOf(
                navArgument("convocazioneId") { type = NavType.IntType },
                navArgument("categoria") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("convocazioneId") ?: return@composable
            val cat = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("categoria") ?: "", "UTF-8"
            )
            ConvocazioneDetailScreen(
                convocazioneId = id,
                categoria = cat,
                viewModel = convocazioniViewModel,
                personeViewModel = personeViewModel,
                onIndietro = { navController.popBackStack() }
            )
        }

        composable(Rotte.LISTA) {
            PersonListScreen(
                viewModel = personeViewModel,
                ruoloUtente = authViewModel.ruolo,
                onPersonaClick = { id -> navController.navigate(Rotte.dettaglio(id)) },
                onNuovaPersona = { navController.navigate(Rotte.NUOVA) },
                onConfigurazione = { navController.navigate(Rotte.CONFIG) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Rotte.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Rotte.DETTAGLIO,
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("personaId") ?: return@composable
            PersonDetailScreen(
                personaId = id,
                viewModel = personeViewModel,
                proviniViewModel = proviniViewModel,
                ruoloUtente = authViewModel.ruolo,
                onIndietro = { navController.popBackStack() },
                onModifica = { navController.navigate(Rotte.modifica(id)) },
                onApriElencoProvini = { personaId ->
                    navController.navigate(Rotte.elencoProvini(personaId))
                },
                onEliminato = {
                    navController.popBackStack(Rotte.LISTA, inclusive = false)
                    personeViewModel.caricaLista()
                }
            )
        }

        composable(
            route = Rotte.ELENCO_PROVINI,
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("personaId") ?: return@composable
            ElencoProviniScreen(
                personaId = id,
                personeViewModel = personeViewModel,
                proviniViewModel = proviniViewModel,
                onIndietro = { navController.popBackStack() },
                onApriProvino = { provinoId ->
                    navController.navigate(Rotte.dettaglioProvino(provinoId))
                }
            )
        }

        composable(
            route = Rotte.DETTAGLIO_PROVINO,
            arguments = listOf(navArgument("provinoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("provinoId") ?: return@composable
            ProvinoDetailScreen(
                provinoId = id,
                proviniViewModel = proviniViewModel,
                onIndietro = { navController.popBackStack() }
            )
        }

        composable(Rotte.NUOVA) {
            PersonFormScreen(
                personaId = null,
                viewModel = personeViewModel,
                onIndietro = { navController.popBackStack() },
                onSalvato = {
                    navController.popBackStack(Rotte.LISTA, inclusive = false)
                    personeViewModel.caricaLista()
                }
            )
        }

        composable(
            route = Rotte.MODIFICA,
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("personaId") ?: return@composable
            PersonFormScreen(
                personaId = id,
                viewModel = personeViewModel,
                onIndietro = { navController.popBackStack() },
                onSalvato = {
                    navController.popBackStack(Rotte.LISTA, inclusive = false)
                    personeViewModel.caricaLista()
                }
            )
        }

        composable(Rotte.CONFIG) {
            ConfigScreen(
                viewModel = configViewModel,
                personeViewModel = personeViewModel,
                onIndietro = { navController.popBackStack() },
                onDatiCambiati = {
                    personeViewModel.caricaLista()
                    personeViewModel.caricaCampiCustom()
                }
            )
        }
    }
    }
}
