# Scouting Persone – App Android

Progetto Kotlin + Jetpack Compose che si collega al backend Flask su PythonAnywhere
(`https://scoutingsampdoria.pythonanywhere.com/`). Include le 4 schermate richieste:
login, lista con ricerca, dettaglio, form di inserimento/modifica.

## Come aprirlo

1. Apri Android Studio (versione consigliata: Koala o successiva).
2. **Open** → seleziona la cartella `ScoutingApp` (quella che contiene `settings.gradle.kts`).
3. Alla prima apertura, Android Studio potrebbe segnalare che manca il file
   `gradle-wrapper.jar` (l'ho omesso perché il mio ambiente non ha accesso a
   internet per scaricarlo): in quel caso Android Studio propone da solo di
   rigenerarlo ("Gradle wrapper not found, use local Gradle distribution?" oppure
   un prompt di sync automatico) — accetta, oppure genera tu il wrapper con:
   ```bash
   gradle wrapper --gradle-version 8.7
   ```
   lanciato dentro la cartella del progetto, se hai Gradle installato in locale.
4. Lascia sincronizzare il progetto (scarica le dipendenze Compose/Retrofit al
   primo avvio, ci vuole qualche minuto).
5. Esegui su un emulatore o dispositivo reale (minSdk 24, quindi va bene quasi
   ogni telefono recente).

## Configurazione dell'URL del backend

L'indirizzo dell'API è impostato in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://scoutingsampdoria.pythonanywhere.com/\"")
```

Se in futuro cambi dominio o passi a un piano con dominio custom, modifica solo
questa riga e ricompila.

## Struttura del progetto

```
app/src/main/java/com/scoutingsampdoria/persone/
├── MainActivity.kt              punto di ingresso, avvia Compose
├── data/
│   ├── model/Models.kt          data class che rispecchiano le risposte JSON dell'API
│   ├── network/PersoneApi.kt    interfaccia Retrofit con tutti gli endpoint
│   ├── network/ApiClient.kt     configurazione Retrofit/OkHttp
│   └── TokenManager.kt          salvataggio del token JWT tramite DataStore
├── repository/
│   └── PersoneRepository.kt     centralizza le chiamate API e la gestione errori
├── viewmodel/
│   ├── AuthViewModel.kt         login, logout, ripristino sessione
│   ├── PersoneViewModel.kt      lista, dettaglio, salvataggio, eliminazione
│   └── ViewModelFactory.kt      costruisce i ViewModel con le loro dipendenze
├── navigation/
│   └── NavGraph.kt              collega le 4 schermate con Navigation Compose
└── ui/screens/
    ├── LoginScreen.kt
    ├── PersonListScreen.kt      con barra di ricerca live
    ├── PersonDetailScreen.kt    modifica/elimina visibili solo in base al ruolo
    └── PersonFormScreen.kt      un solo form per creazione E modifica
```

## Permessi in base al ruolo

L'app legge il ruolo restituito dal login (`admin`, `editor`, `viewer`) e lo usa
per mostrare o nascondere i pulsanti di modifica/eliminazione nel dettaglio
persona — la vera validazione resta comunque lato server (il backend rifiuta
comunque le richieste non autorizzate, l'app si limita a non mostrare pulsanti
inutili).

## Sessione persistente

Il token JWT viene salvato con Jetpack DataStore (non SharedPreferences in
chiaro) e recuperato automaticamente alla riapertura dell'app: se è ancora
valido (scade dopo 12 ore, vedi `TOKEN_EXPIRE_HOURS` nel backend), l'utente
salta direttamente alla lista senza rifare il login.

## Prossimi passi possibili

- Aggiungere un pull-to-refresh nella lista.
- Filtri più avanzati (per regione/società/ruolo) con un menu a tendina invece
  di lasciarli solo come parametri API già pronti nel repository.
- Paginazione infinita nella lista (l'API supporta già `page`/`per_page`).
- Un `WorkManager` per rinnovare automaticamente la sessione prima della scadenza.
