package com.example.rentmate

import AuthViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.rentmate.ui.theme.RentMateTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val apartmentViewModel: ApartmentViewModel = viewModel()
            // ✅ ADDED: Initialize the AuthViewModel
            val authViewModel: AuthViewModel = viewModel()

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val authScreens = listOf("welcome", "login", "signup")
            val isDarkTheme = if (currentRoute in authScreens) false else themeViewModel.isDarkMode.value

            RentMateTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    bottomBar = {
                        if (currentRoute != null && currentRoute !in authScreens) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "welcome"
                        ) {
                            composable("welcome") { WelcomeScreen(navController) }
                            composable("login") { LoginScreen(navController) }
                            composable("signup") { SignUpScreen(navController) }
                            composable("user_manual") { UserManualScreen(navController) }
                            composable("listings") { ListingsScreen(navController, apartmentViewModel) }

                            composable(
                                route = "add_listing?editId={editId}",
                                arguments = listOf(navArgument("editId") {
                                    type = NavType.StringType
                                    nullable = true
                                })
                            ) { backStackEntry ->
                                val editId = backStackEntry.arguments?.getString("editId")?.toIntOrNull()
                                AddListingScreen(navController, apartmentViewModel, editId = editId)
                            }

                            composable("profile") { ProfileScreen(navController, apartmentViewModel) }
                            composable(route = "map") { MapScreen(navController = navController, viewModel = apartmentViewModel) }


                            composable("change_password") {
                                ChangePasswordScreen(navController, authViewModel)
                            }

                            composable("privacy_policy") { PrivacyPolicyScreen(navController) }
                            composable("favorites") { FavoritesScreen(navController, apartmentViewModel) }
                            composable("edit_profile") { EditProfileScreen(navController, apartmentViewModel) }
                            composable("notifications") { NotificationsScreen(navController, apartmentViewModel) }
                            composable("settings") { SettingsScreen(navController, themeViewModel) }

                            composable("details/{name}") { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                DetailsScreen(
                                    apartmentName = name,
                                    navController = navController,
                                    viewModel = apartmentViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
