package com.example.rentmate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddListingScreen(navController: NavController, viewModel: ApartmentViewModel, editId: Int? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    val existingApartment = viewModel.listings.find { it.id == editId }

    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") } // ✅ NEW: Phone state
    var addressInput by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }


    val selectedAmenities = remember { mutableStateListOf<String>() }
    val amenitiesList = listOf(
        "Wi-Fi" to Icons.Default.Wifi,
        "Pool" to Icons.Default.Pool,
        "Gym" to Icons.Default.FitnessCenter,
        "Parking" to Icons.Default.LocalParking
    )


    LaunchedEffect(existingApartment) {
        existingApartment?.let { apartment ->
            title = apartment.name
            price = apartment.price.filter { it.isDigit() }
            phone = apartment.phone // ✅ NEW: Load phone from database
            addressInput = apartment.location
            description = apartment.description
            selectedAmenities.clear()
            selectedAmenities.addAll(apartment.amenities)
        }
    }


    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    val isFormValid = title.isNotBlank() && price.isNotBlank() && addressInput.isNotBlank() && !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        Surface(
            color = Color(0xFF0C3540),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (existingApartment != null) "Update Property" else "List Your Property",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
        }

        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable(enabled = !isSaving) {
                        photoPickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || (existingApartment != null && existingApartment.imageUrl.isNotEmpty())) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = selectedImageUri ?: existingApartment?.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (!isSaving) {
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("Add Apartment Photos", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            // --- 📝 INPUT FIELDS ---
            ListingInput(title, { title = it }, "Apartment Title", Icons.Default.Home, enabled = !isSaving)
            ListingInput(price, { price = it }, "Monthly Rent (PHP)", Icons.Default.Payments, KeyboardType.Number, enabled = !isSaving)
            // ✅ NEW: AUTOMATIC PHONE INPUT
            ListingInput(phone, { phone = it }, "Landlord Contact Number", Icons.Default.Phone, KeyboardType.Phone, enabled = !isSaving)
            ListingInput(addressInput, { addressInput = it }, "Address (e.g. BGC, Taguig)", Icons.Default.LocationOn, enabled = !isSaving)
            ListingInput(rooms, { rooms = it }, "Number of Rooms", Icons.Default.Bed, KeyboardType.Number, enabled = !isSaving)

            Text("About This Place", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Describe the unit, view, or nearby spots...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = Color(0xFF0C3540)
                )
            )

            Text("Select Amenities", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                amenitiesList.forEach { (name, icon) ->
                    val isSelected = selectedAmenities.contains(name)
                    SelectableAmenity(
                        icon = icon,
                        label = name,
                        isSelected = isSelected,
                        enabled = !isSaving,
                        onClick = {
                            if (isSelected) selectedAmenities.remove(name)
                            else selectedAmenities.add(name)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = {
                    if (isFormValid) {
                        isSaving = true
                        coroutineScope.launch {
                            val newPriceString = "PHP $price/mo"
                            val searchAddress = if (addressInput.lowercase().contains("taguig")) addressInput else "$addressInput, Taguig City"
                            val foundLatLng = getCoordinatesFromAddress(context, searchAddress)
                            val result = foundLatLng ?: LatLng(14.5486, 121.0500)

                            // ✅ AUTOMATIC: Update existing or Create new with Phone Number
                            val finalApartment = existingApartment?.copy(
                                name = title,
                                previousPrice = if (newPriceString != existingApartment.price) existingApartment.price else existingApartment.previousPrice,
                                price = newPriceString,
                                phone = phone, // ✅ Save phone
                                location = addressInput,
                                description = description,
                                amenities = selectedAmenities.toList(),
                                lat = result.latitude,
                                lng = result.longitude
                            ) ?: Apartment(
                                id = (System.currentTimeMillis() % 10000).toInt(),
                                name = title,
                                price = newPriceString,
                                phone = phone, // ✅ Save phone
                                location = addressInput,
                                ownerEmail = "oninsandrade@gmail.com",
                                lat = result.latitude,
                                lng = result.longitude,
                                description = description,
                                amenities = selectedAmenities.toList()
                            )

                            viewModel.addApartment(finalApartment, selectedImageUri)

                            delay(1500)
                            isSaving = false
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFF0C3540) else Color.Red
                )
            ) {
                if (isSaving) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Saving Changes...")
                    }
                } else {
                    Text(if (existingApartment != null) "Update & Notify Users" else "Post Listing Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SelectableAmenity(icon: ImageVector, label: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled) { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) Color(0xFF0C3540) else MaterialTheme.colorScheme.surfaceVariant,
            border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(14.dp),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = if (isSelected) Color(0xFF0C3540) else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ListingInput(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, enabled: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF0C3540)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0C3540),
            focusedLabelColor = Color(0xFF0C3540)
        )
    )
}

@Suppress("DEPRECATION")
fun getCoordinatesFromAddress(context: android.content.Context, address: String): LatLng? {
    return try {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        val fullAddress = "$address, Signal Village, Taguig, Philippines"
        val results = geocoder.getFromLocationName(fullAddress, 1)

        if (!results.isNullOrEmpty()) {
            val result = results[0]
            LatLng(result.latitude, result.longitude)
        } else null
    } catch (e: Exception) {
        android.util.Log.e("GeocoderError", "Error finding address: ${e.message}")
        null
    }
}
