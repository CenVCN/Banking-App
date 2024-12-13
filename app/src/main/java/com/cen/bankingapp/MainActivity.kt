// MainActivity.kt
package com.cen.bankingapp

import SignupScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cen.bankingapp.ui.theme.BankningAppUITheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            BankningAppUITheme {
                SetBarColor(color = MaterialTheme.colorScheme.background)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }

    @Composable
    private fun SetBarColor(color: Color) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color
            )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {
        val navController = rememberNavController()
        var showLoadingScreen by remember { mutableStateOf(false) }
        var showHomeScreen by remember { mutableStateOf(false) }
        var showLoginScreen by remember { mutableStateOf(true) }
        var jwtToken by remember { mutableStateOf<String?>(null) }
        var currentRoute by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(navController) {
            navController.currentBackStackEntryFlow.collect { backStackEntry ->
                currentRoute = backStackEntry.destination.route
            }
        }

        Scaffold(
            bottomBar = {
                if (currentRoute !in listOf("loginScreen", "signupScreen")) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) {
            if (showLoadingScreen) {
                LoadingScreen()
            } else {
                NavHost(navController = navController, startDestination = "loginScreen") {
                    composable("homeScreen") {
                        HomeScreen(navController = navController, onAddCard = { navController.navigate("addCardScreen") })
                    }
                    composable("addCardScreen") {
                        AddCardScreen(navController) {
                            navController.popBackStack()
                        }
                    }
                    composable("walletScreen") {
                        WalletScreen(
                            onSendBalance = { /* Handle send balance */ },
                            onAddBalance = { /* Handle add balance */ },
                            onWithdrawBalance = { /* Handle withdraw balance */ },
                            navController = navController
                        )
                    }
                    composable("transactionsScreen") {
                        TransactionsScreen(navController)
                    }
                    composable("accountScreen") {
                        ProfileScreen(navController)
                    }
                    composable("loginScreen") {
                        LoginScreen(
                            onLogin = { email, token ->
                                showLoadingScreen = true
                                // Simulate a network call delay
                                lifecycleScope.launch {
                                    kotlinx.coroutines.delay(2000)
                                    jwtToken = token
                                    showLoadingScreen = false
                                    showHomeScreen = true
                                    showLoginScreen = false
                                    navController.navigate("homeScreen") // Navigate to home screen
                                }
                            },
                            onSignupRedirect = {
                                navController.navigate("signupScreen")
                            }
                        )
                    }
                    composable("signupScreen") {
                        SignupScreen {
                            navController.navigate("loginScreen")
                        }
                    }
                }
            }
        }
    }

//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun MainContent() {
//        val navController = rememberNavController()
//        var showHomeScreen by remember { mutableStateOf(false) }
//        var showLoginScreen by remember { mutableStateOf(true) }
//        var showSignupScreen by remember { mutableStateOf(false) }
//        var showLoadingScreen by remember { mutableStateOf(false) }
//        var jwtToken by remember { mutableStateOf<String?>(null) }
//
//        Scaffold(
//            bottomBar = {
//                if (!showLoadingScreen && !showLoginScreen && !showSignupScreen) {
//                    BottomNavigationBar(navController = navController)
//                }
////                if (navController.currentDestination?.route != "loginScreen" && navController.currentDestination?.route != "signupScreen") {
////                    BottomNavigationBar(navController = navController)
////                }
//            }
//        ) {
//            NavHost(navController = navController, startDestination = "homeScreen") {
//
////                composable("homeScreen") {
////                    HomeScreen(navController = navController) {
////                        navController.navigate("addCardScreen")
////                    }
////                }
//                composable("homeScreen") {
//                    when {
//                        showLoadingScreen -> {
//                            LoadingScreen()
//                        }
//                        showHomeScreen -> {
//                            HomeScreen(navController = navController, onAddCard = { navController.navigate("addCardScreen") })
//                        }
//                        showLoginScreen -> {
//                            LoginScreen(
//                                onLogin = { email, token ->
//                                    showLoadingScreen = true
//                                    // Simulate a network call delay
//                                    kotlinx.coroutines.GlobalScope.launch {
//                                        kotlinx.coroutines.delay(2000)
//                                        jwtToken = token
//                                        showLoadingScreen = false
//                                        showHomeScreen = true
//                                        showLoginScreen = false
//                                    }
//                                },
//                                onSignupRedirect = {
//                                    showLoginScreen = false
//                                    showSignupScreen = true
//                                }
//                            )
//                        }
//                        showSignupScreen -> {
//                            SignupScreen {
//                                showSignupScreen = false
//                                showLoginScreen = true
//                            }
//                        }
//                    }
//                }
//                composable("addCardScreen") {
//                    AddCardScreen(navController) {
//                        navController.popBackStack()
//                    }
//                }
//                composable("walletScreen") {
//                    WalletScreen(
//                        onSendBalance = { /* Handle send balance */ },
//                        onAddBalance = { /* Handle add balance */ },
//                        onWithdrawBalance = { /* Handle withdraw balance */ },
//                        navController = navController
//                    )
//                }
//                composable("transactionsScreen") {
//                    TransactionsScreen(navController)
//                }
//                composable("accountScreen") {
//                    ProfileScreen(navController)
//                }
//                composable("loginScreen") {
//                    showLoginScreen = true
//                    showSignupScreen = false
//                    LoginScreen(
//                        onLogin = { email, token ->
//                            showLoadingScreen = true
//                            // Simulate a network call delay
//                            kotlinx.coroutines.GlobalScope.launch {
//                                kotlinx.coroutines.delay(2000)
//                                jwtToken = token
//                                showLoadingScreen = false
//                                showHomeScreen = true
//                                showLoginScreen = false
//                            }
//                        },
//                        onSignupRedirect = {
//                            showLoginScreen = false
//                            showSignupScreen = true
//                        }
//                    )
//                }
//                composable("signupScreen") {
//                    SignupScreen {
//                        showSignupScreen = false
//                        showLoginScreen = true
//                }
//            }
//        }
//    }
//    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen(navController: NavController, onAddCard: () -> Unit) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                WalletSection()
                CardsSection(onAddCard = onAddCard)
                Spacer(modifier = Modifier.height(16.dp))
                FinanceSection(navController = navController)
                CurrenciesSection()
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("walletScreen") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Go to Wallet")
                }
            }
        }
    }
}



