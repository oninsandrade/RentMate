package com.example.rentmate

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun DetailsScreen(apartmentName: String, navController: NavController, viewModel: ApartmentViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 🔍 Find the specific apartment from your live Firestore list
    val apartment = viewModel.listings.find { it.name == apartmentName }

    // Fallback if apartment isn't found (prevents crash)
    if (apartment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // --- 🖼️ IMAGE GALLERY ---
        Box {
            LazyRow(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                items(3) {
                    AsyncImage(
                        model = apartment.imageUrl.ifEmpty { R.drawable.welcome_bg },
                        contentDescription = null,
                        modifier = Modifier.fillParentMaxWidth().height(320.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.welcome_bg),
                        error = painterResource(id = R.drawable.welcome_bg)
                    )
                }
            }

            // ⬅️ BACK BUTTON
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            val currentUser = "oninsandrade@gmail.com"
            if (apartment.ownerEmail == currentUser) {
                IconButton(
                    onClick = {
                        // Navigation to the Edit Flow
                        navController.navigate("add_listing?editId=${apartment.id}")
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, "Edit Listing", tint = Color.White)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {

            Text(
                text = apartment.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = apartment.price,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 📍 LOCATION ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = apartment.location,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Amenities", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                apartment.amenities.forEach { amenity ->
                    val icon = when(amenity) {
                        "Wi-Fi" -> Icons.Default.Wifi
                        "Pool" -> Icons.Default.Pool
                        "Gym" -> Icons.Default.FitnessCenter
                        else -> Icons.Default.LocalParking
                    }
                    AmenityItem(icon, amenity)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 📝 ABOUT THIS PLACE ---
            Text(
                "About This Place",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = apartment.description.ifEmpty { "No description provided." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))


            Button(
                onClick = {

                    val ownerPhone = apartment.phone.ifEmpty { "09123456789" }
                    val intent = Intent(Intent.ACTION_DIAL, "tel:$ownerPhone".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C3540), // Your Signature Teal
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Phone, null)
                Spacer(Modifier.width(12.dp))
                Text("Contact Owner Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AmenityItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(14.dp),
                tint = Color(0xFF0C3540)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
