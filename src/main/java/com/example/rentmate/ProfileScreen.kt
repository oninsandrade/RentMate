package com.example.rentmate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ApartmentViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser


    var username by remember { mutableStateOf("Loading...") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) } // 🚀 NEW: State for the photo URL
    val email = currentUser?.email ?: "No Email Found"


    DisposableEffect(currentUser?.uid, viewModel) {
        val uid = currentUser?.uid
        if (uid == null) {
            username = "Guest User"
            return@DisposableEffect onDispose {}
        }

        val listener = db.collection("users").document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    username = "Error Loading"
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    username = document.getString("username") ?: "New User"
                    // 🚀 THE FIX: Fetches the 'profileImage' URL from Firestore
                    profileImageUrl = document.getString("profileImage")
                } else {
                    username = "User Profile"
                }
            }

        onDispose { listener.remove() }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {


        Surface(
            color = Color(0xFF0C3540),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(top = 48.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {

                    if (!profileImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {

                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = email,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }


        Column(modifier = Modifier.padding(24.dp)) {
            ProfileMenuItem(Icons.Default.Edit, "Edit Profile") {
                navController.navigate("edit_profile")
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            ProfileMenuItem(Icons.Default.Notifications, "Notifications") {
                navController.navigate("notifications")
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            ProfileMenuItem(Icons.Default.Settings, "Settings") {
                navController.navigate("settings")
            }
            // ✨ ADD THIS NEW ITEM
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            ProfileMenuItem(Icons.AutoMirrored.Filled.HelpOutline, "User Manual") {
                navController.navigate("user_manual")
            }

            Spacer(modifier = Modifier.height(32.dp))





        Button(
                onClick = { navController.navigate("add_listing") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddHome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("List a New Property", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("welcome") {
                        popUpTo(0) // 🚀 Clears navigation history for security
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    contentColor = Color.Red
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0C3540), modifier = Modifier.size(22.dp))

        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp).weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
