package com.example.rentmate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.PropertyName

// --- 🏗️ THE DATA MODEL ---
data class Apartment(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val phone: String = "",
    val previousPrice: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val ownerEmail: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val description: String = "",
    val amenities: List<String> = emptyList(),
    @get:PropertyName("favorite")
    @set:PropertyName("favorite")
    var favorite: Boolean = false
)

@Composable
fun ListingsScreen(navController: NavController, viewModel: ApartmentViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val taguigListings = viewModel.listings
    val isLoading = viewModel.isLoading

    val filteredListings = if (searchQuery.isEmpty()) {
        taguigListings
    } else {
        taguigListings.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Apartments in Taguig") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Text(
            text = "Rentals in Taguig",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )


        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isLoading && taguigListings.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredListings) { apartment ->
                        ApartmentCard(
                            apartment = apartment,
                            viewModel = viewModel,
                            navController = navController,
                            onClick = { navController.navigate("details/${apartment.name}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApartmentCard(
    apartment: Apartment,
    viewModel: ApartmentViewModel,
    navController: NavController,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }


    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email?.lowercase()?.trim()
    val isOwner = apartment.ownerEmail.lowercase().trim() == currentUserEmail

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing?") },
            text = { Text("Are you sure you want to remove '${apartment.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteApartment(apartment)
                    showDeleteDialog = false
                }) { Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(
                    model = if (apartment.imageUrl.isNotEmpty()) apartment.imageUrl else R.drawable.welcome_bg,
                    contentDescription = apartment.name,
                    modifier = Modifier.fillMaxSize().clickable { onClick() },
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(48.dp)
                        .clickable { viewModel.toggleFavorite(apartment) },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (apartment.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (apartment.favorite) Color.Red else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).clickable { onClick() }) {
                    Text(apartment.name, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(apartment.price, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("LIVE", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Text(apartment.location, color = Color.Gray, fontSize = 13.sp)
                }


                if (isOwner) {
                    Row {
                        IconButton(onClick = {
                            navController.navigate("add_listing?editId=${apartment.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
