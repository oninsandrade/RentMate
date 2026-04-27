package com.example.rentmate

import AuthViewModel
import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class ApartmentViewModel : ViewModel() {

    private val repository = ApartmentRepository()
    private val authViewModel = AuthViewModel() // 🔐 Linked for secure Auth operations

    val listings = mutableStateListOf<Apartment>()

    var isLoading by mutableStateOf(false)
        private set

    init {
        observeListings()
    }

    private fun observeListings() {
        viewModelScope.launch {
            // 🔄 Real-time collection from Firestore
            repository.getListingsFlow().collect { updatedList ->
                // ✅ STABILITY FIX: Prevents heart icon flickering and unnecessary UI jumps
                if (listings.toList() != updatedList) {
                    listings.clear()
                    listings.addAll(updatedList)
                }
                isLoading = false
            }
        }
    }

    // 🚀 UPDATED LIVE NOTIFICATIONS: Handles both Price Drops and New Listings
    val liveNotifications = derivedStateOf {
        listings.map { apartment ->
            if (apartment.previousPrice.isNotEmpty() && isPriceLower(apartment.price, apartment.previousPrice)) {
                // 💸 Case 1: Price Drop
                NotificationItem(
                    title = "Price Drop! 💸",
                    message = "Great news! '${apartment.name}' in ${apartment.location} is now cheaper!",
                    time = "Just now"
                )
            } else {
                // 🏠 Case 2: New Listing (Shown for fresh posts)
                NotificationItem(
                    title = "New Listing! 🏠",
                    message = "A brand new property '${apartment.name}' was just posted in ${apartment.location}!",
                    time = "Recently"
                )
            }
        }.reversed() // 🔄 Show newest at the top
    }

    private fun isPriceLower(current: String, previous: String): Boolean {
        val curNum = current.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        val prevNum = previous.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        return curNum < prevNum
    }

    fun toggleFavorite(apartment: Apartment) {
        viewModelScope.launch {
            val isFavorite = !apartment.favorite
            val index = listings.indexOfFirst { it.id == apartment.id }
            if (index != -1) {
                listings[index] = apartment.copy(favorite = isFavorite)
            }
            repository.toggleFavorite(apartment.id.toString(), isFavorite)
        }
    }

    fun addApartment(apartment: Apartment, imageUri: Uri?) {
        viewModelScope.launch {
            isLoading = true
            val foundLatLng = getExactCoordinates(apartment.location)
            var finalImageUrl = apartment.imageUrl

            if (imageUri != null) {
                val uploadedUrl = repository.uploadImage(imageUri, apartment.id)
                if (uploadedUrl != null) {
                    finalImageUrl = uploadedUrl
                }
            }

            val updatedApartment = apartment.copy(
                imageUrl = finalImageUrl,
                lat = foundLatLng?.latitude ?: apartment.lat,
                lng = foundLatLng?.longitude ?: apartment.lng
            )

            repository.saveApartment(updatedApartment)
            isLoading = false
        }
    }


    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // Match the capitalization here:
            val authVm = AuthViewModel()
            authVm.changePassword(oldPass, newPass) { success, message ->
                onResult(success, message)
            }
        }
    }



    fun updateProfile(newName: String, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val success = repository.updateProfile(newName, imageUri)
            onComplete(success)
            isLoading = false
        }
    }

    fun deleteApartment(apartment: Apartment) {
        viewModelScope.launch {
            repository.deleteApartment(apartment.id.toString())
        }
    }
}

// 🗺️ Geocoding function kept as requested
suspend fun getExactCoordinates(address: String): LatLng? = withContext(Dispatchers.IO) {
    try {
        // 🔒 Note: For production, move this to local.properties or BuildConfig
        val apiKey = "AIzaSyCXLsPFaV409tl7msJxHa_lLmMqbW5E20A"
        val fullQuery = "$address, Central Signal Village, Taguig, Philippines"
        val encodedQuery = URLEncoder.encode(fullQuery, "UTF-8")

        val url = URL("https://googleapis.com")

        val response = url.readText()
        val json = JSONObject(response)

        if (json.getString("status") == "OK") {
            val loc = json.getJSONArray("results").getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
            LatLng(loc.getDouble("lat"), loc.getDouble("lng"))
        } else {
            null
        }
    } catch (e: Exception) {
        println("Geocoding Error: ${e.message}")
        null
    }
}
