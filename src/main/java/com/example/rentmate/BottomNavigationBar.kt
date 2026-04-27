package com.example.rentmate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector // ✅ Added missing import
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// 🏗️ Step 1: Define the Data Model (This fixes the "Unresolved NavItem" errors)
data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    // 🛠️ Step 2: Initialize the list of tabs
    val items = listOf(
        NavItem("listings", "Home", Icons.Filled.Home),
        NavItem("map", "Map", Icons.Filled.Place),
        NavItem("favorites", "Favorites", Icons.Filled.Favorite),
        NavItem("profile", "Profile", Icons.Filled.Person)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Avoid multiple copies of the same destination when reselecting the same item
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
