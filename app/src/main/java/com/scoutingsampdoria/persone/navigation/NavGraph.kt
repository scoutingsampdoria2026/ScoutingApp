package com.scoutingsampdoria.persone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scoutingsampdoria.persone.ui.screens.ConfigScreen
import com.scoutingsampdoria.persone.ui.screens.HomeScreen
import com.scoutingsampdoria.persone.ui.screens.LoginScreen
import com.scoutingsampdoria.persone.ui.screens.PersonDetailScreen
import com.scoutingsampdoria.persone.ui.screens.PersonFormScreen
import com.scoutingsampdoria.persone.ui.screens.PersonListScreen
import com.scoutingsampdoria.persone.viewmodel.AuthViewModel
import com.scoutingsampdoria.persone.viewmodel.ConfigViewModel
import com.scoutingsampdoria.persone.viewmodel.PersoneViewModel
import com.scoutingsampdoria.persone.viewmodel.ViewModelFactory

private object Rotte {
    const val LOGIN = "login"
    const val HOME = "home"
    const val LISTA = "lista"
    const val DETTAGLIO = "dettaglio/{personaId}"
    const val NUOVA = "nuova"
    const val MODIFICA = "modifica/{personaId}"
    const val CONFIG = "config"

    fun dettaglio(id: Int) = "dettaglio/$id"
    fun modifica(id: Int) = "modifica/$id"
}

@Composable
fun ScoutingNavGraph(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val personeViewModel: PersoneViewModel = viewModel(factory = factory)
    val configViewModel: ConfigViewModel = viewModel(factory = factory)

    var sessioneVerificata by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.ripristinaSessione { haSessione ->
            sessioneVerificata = true
        }
    }

    if (!sessioneVerificata) return

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
                ruoloUtente = authViewModel.ruolo,
                onIndietro = { navController.popBackStack() },
                onModifica = { navController.navigate(Rotte.modifica(id)) },
                onEliminato = {
                    navController.popBackStack(Rotte.LISTA, inclusive = false)
                    personeViewModel.caricaLista()
                }
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
