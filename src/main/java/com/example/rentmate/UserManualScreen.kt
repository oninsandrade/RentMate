package com.example.rentmate

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController

@Composable
fun UserManualScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 🏗️ TOP BAR ---
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
                Text("User Manual", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- 📖 MANUAL CONTENT ---
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ManualSection("🔍 Finding Your Home", "Browse the 'Home' tab for Taguig listings. Use the search bar to find areas like BGC or Signal Village.")

            ManualSection("📍 Using the Map", "The 'Map' tab shows properties as price tags. Tapping a tag shows a quick preview card. Pulsing green tags are special deals!")

            ManualSection("❤️ Favorites", "Tap the heart on any listing to save it. View your saved properties in the 'Favorites' tab for easy comparison.")

            ManualSection("🏠 For Landlords", "Tap 'List a New Property' in your profile to add your unit. Editing your price later triggers a 'Price Drop' alert for others!")

            Spacer(Modifier.height(32.dp))

            // --- ✉️ CONTACT SUPPORT ---
            Text("Still need help?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:support@rentmate.com".toUri()
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Email, null, tint = Color(0xFF0C3540))
                Spacer(Modifier.width(12.dp))
                Text("Email Support", color = Color(0xFF0C3540), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ManualSection(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0C3540))
        Spacer(Modifier.height(6.dp))
        Text(description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
    }
}
