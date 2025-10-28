package com.example.afapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.afapp.navigation.AppNavGraph
import com.example.afapp.ui.theme.afappAppTheme
import com.example.afapp.ui.viewmodel.AuthViewModel
import com.example.afapp.ui.viewmodel.AuthViewModelFactory
import com.example.afapp.ui.viewmodel.SessionViewModel
import com.example.afapp.ui.viewmodel.SessionViewModelFactory
import com.example.afapp.utils.ServiceLocator

class MainActivity : ComponentActivity() {

    // AuthViewModel se inicializa correctamente usando su factoría.
    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(application) }

    // SessionViewModel necesita el UserRepository, que obtenemos a través del ServiceLocator.
    private val sessionViewModel: SessionViewModel by viewModels {
        val userRepo = ServiceLocator.provideUserRepository(application)
        SessionViewModelFactory(userRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            afappAppTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    sessionViewModel = sessionViewModel
                )
            }
        }
    }
}
