package com.scoutingsampdoria.persone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.scoutingsampdoria.persone.navigation.ScoutingNavGraph
import com.scoutingsampdoria.persone.ui.theme.SampdoriaTheme
import com.scoutingsampdoria.persone.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = ViewModelFactory(applicationContext)

        setContent {
            SampdoriaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScoutingNavGraph(factory = factory)
                }
            }
        }
    }
}
