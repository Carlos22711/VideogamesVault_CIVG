package com.gneticcore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gneticcore.app.ui.GneticCoreNavGraph
import com.gneticcore.app.ui.GneticCoreTheme
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.UserSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar la sesión persistente antes de que se renderice nada
        UserSession.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            GneticCoreTheme {
                val gameViewModel: GameViewModel = viewModel()
                GneticCoreNavGraph(viewModel = gameViewModel)
            }
        }
    }
}
