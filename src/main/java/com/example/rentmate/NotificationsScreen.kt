package com.example.rentmate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * 🔔 RENTMATE NOTIFICATIONS SCREEN
 *
 * This screen is now fully "Lively." It observes the [ApartmentViewModel.liveNotifications]
 * which reacts in real-time to any price changes or new listings in your Firestore database.
 */

// ✅ DATA MODEL: Defined at the top level so it is accessible to the whole file
data class NotificationItem(
    val title: String,
    val message: String,
    val time: String
)

@Composable
fun NotificationsScreen(navController: NavController, viewModel: ApartmentViewModel) {
    // 📥 LIVE SYNC: This variable automatically updates whenever your Firestore data changes
    val notifications = viewModel.liveNotifications.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 🏗️ HEADER ---
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 🏠 CONTENT AREA ---
        if (notifications.isEmpty()) {
            // State when no activity is detected in Firestore
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        text = "No notifications yet",
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Live list of Price Drops and New Listings
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
            ) {
                items(notifications) { item ->
                    NotificationRow(item)
                }
            }
        }
    }
}

@Composable
fun NotificationRow(notification: NotificationItem) {
    // 🎨 LIVELY LOGIC:
    // Green theme for Price Drops, Blue theme for New Listings/General
    val isPriceDrop = notification.title.contains("Price Drop", ignoreCase = true)
    val themeColor = if (isPriceDrop) Color(0xFF4CAF50) else Color(0xFF2196F3)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✨ Animated-look Circle Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(themeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPriceDrop) Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = notification.time,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
