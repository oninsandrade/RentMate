package com.example.rentmate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController, themeViewModel: ThemeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- TOP BAR ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(top = 40.dp, bottom = 16.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            // --- GENERAL SECTION ---
            Text("General", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))

            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                label = "Dark Mode",
                isChecked = themeViewModel.isDarkMode.value,
                onCheckedChange = { themeViewModel.toggleDarkMode(it) }
            )

            Spacer(Modifier.height(32.dp))

            // --- ACCOUNT SECTION ---
            Text("Account", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))

            // ✅ Navigation: Change Password
            ProfileMenuItem(Icons.Default.Lock, "Change Password") {
                navController.navigate("change_password")
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            // ✅ Navigation: Privacy Policy
            ProfileMenuItem(Icons.Default.Info, "Privacy Policy") {
                navController.navigate("privacy_policy")
            }
        }
    }
}

@Composable
fun SettingsToggleItem(icon: ImageVector, label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp).weight(1f),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
