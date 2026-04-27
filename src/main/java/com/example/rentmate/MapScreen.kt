package com.example.rentmate

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun MapScreen(navController: NavController, viewModel: ApartmentViewModel) {
    val context = LocalContext.current
    val listings = viewModel.listings
    var selectedFilter by remember { mutableStateOf("All") }

    // ✨ LIVELY STATE: Track which apartment is currently "peeked"
    var selectedApartment by remember { mutableStateOf<Apartment?>(null) }

    // --- 🛡️ 1. PERMISSIONS ---
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) { launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }

    // --- 🗺️ 2. MAP STATE ---
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(listings) {
        if (listings.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            listings.forEach { builder.include(LatLng(it.lat, it.lng)) }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(builder.build(), 200))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 🗺️ 3. GOOGLE MAP ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission),
            onMapClick = { selectedApartment = null }
        ) {
            listings.forEach { apartment ->
                // ✅ REPLACED LOGIC: Smarter filtering for room types
                val isVisible = when (selectedFilter) {
                    "All" -> true
                    "Studio" -> apartment.name.contains("Studio", ignoreCase = true)
                    "1-Bedroom" -> apartment.name.contains("1", ignoreCase = true)
                    "2-Bedrooms" -> apartment.name.contains("2", ignoreCase = true)
                    else -> true
                }


                if (isVisible) {
                    val position = LatLng(apartment.lat, apartment.lng)
                    val markerState = rememberMarkerState(position = position)

                    // ✨ LIVELY PULSE LOGIC: Create pulse for "Price Drop" deals
                    val isDeal = apartment.id % 2 == 0 // Example logic: Every 2nd listing is a "deal"
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    MarkerComposable(
                        state = markerState,
                        anchor = Offset(0.5f, 1f),
                        onClick = {
                            selectedApartment = apartment
                            true
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.graphicsLayer {
                                    if (isDeal) {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                // 🟢 Lively Green for Deals, Teal for Normal, Blue for Selected
                                color = when {
                                    selectedApartment?.id == apartment.id -> Color(0xFF2196F3)
                                    isDeal -> Color(0xFF4CAF50)
                                    else -> Color(0xFF0C3540)
                                },
                                shadowElevation = 8.dp,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = apartment.price.split("/").first(),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            // Pointer "Beak"
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .offset(y = (-5).dp)
                                    .graphicsLayer { rotationZ = 45f }
                                    .background(
                                        when {
                                            selectedApartment?.id == apartment.id -> Color(0xFF2196F3)
                                            isDeal -> Color(0xFF4CAF50)
                                            else -> Color(0xFF0C3540)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }


        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val filterOptions = listOf("All", "Studio", "1-Bedroom", "2-Bedrooms")

            filterOptions.forEach { filter ->
                val isSelected = selectedFilter == filter

                // ✨ ANIMATION: Smoothly transitions the background color
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF2196F3) else Color.White,
                    animationSpec = tween(300)
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF0C3540),
                    animationSpec = tween(300)
                )

                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    shadowElevation = if (isSelected) 6.dp else 0.dp,
                    modifier = Modifier.height(42.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }


        // --- 🏠 5. LIVELY PREVIEW CARD ---
        AnimatedVisibility(
            visible = selectedApartment != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) {
            selectedApartment?.let { apartment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(115.dp)
                        .clickable { navController.navigate("details/${apartment.name}") },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(15.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        AsyncImage(
                            model = apartment.imageUrl.ifEmpty { R.drawable.welcome_bg },
                            contentDescription = null,
                            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(apartment.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                            Text(apartment.price, color = Color(0xFF2196F3), fontWeight = FontWeight.ExtraBold)
                            Text(apartment.location, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                        }

                        IconButton(
                            onClick = { selectedApartment = null },
                            modifier = Modifier.align(Alignment.Top).size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
