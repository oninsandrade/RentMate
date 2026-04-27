package com.example.rentmate

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ApartmentRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage.reference
    private val listingsCollection = db.collection("listings")


    suspend fun updateProfile(newName: String, imageUri: Uri?): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val updates = mutableMapOf<String, Any>("username" to newName)

            if (imageUri != null) {
                // Uploads profile pic to Storage folder 'profiles/'
                val ref = storage.child("profiles/$uid.jpg")
                ref.putFile(imageUri).await()
                updates["profileImage"] = ref.downloadUrl.await().toString()
            }

            db.collection("users").document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            println("Profile update error: ${e.message}")
            false
        }
    }


    suspend fun toggleFavorite(apartmentId: String, isFavorite: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val favRef = db.collection("users").document(uid).collection("favorites").document(apartmentId)
        try {
            if (isFavorite) favRef.set(mapOf("id" to apartmentId)).await()
            else favRef.delete().await()
        } catch (e: Exception) {
            println("Favorite toggle error: ${e.message}")
        }
    }


    fun getListingsFlow(): Flow<List<Apartment>> = callbackFlow {
        val uid = auth.currentUser?.uid


        val listener = listingsCollection.addSnapshotListener { snapshot, _ ->


            if (uid != null) {
                db.collection("users").document(uid).collection("favorites")
                    .addSnapshotListener { favSnapshot, _ ->
                        // Get IDs the user personally hearted
                        val favIds = favSnapshot?.documents?.map { it.id }?.toSet() ?: emptySet()

                        // Convert Firestore documents to Apartment objects
                        val apartments = snapshot?.toObjects(Apartment::class.java) ?: emptyList()

                        // ✅ SYNC: Force heart to stay red if ID is in user's favorites
                        val syncedList = apartments.map {
                            it.copy(favorite = it.id.toString() in favIds)
                        }
                        trySend(syncedList)
                    }
            } else {
                // If not logged in, just show global listings
                val apartments = snapshot?.toObjects(Apartment::class.java) ?: emptyList()
                trySend(apartments)
            }
        }


        awaitClose { listener.remove() }
    }


    suspend fun uploadImage(uri: Uri, apartmentId: Int): String? {
        return try {
            val fileRef = storage.child("apartments/$apartmentId.jpg")
            fileRef.putFile(uri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            println("Upload error: ${e.message}")
            null
        }
    }


    suspend fun saveApartment(apartment: Apartment): Boolean {
        return try {
            listingsCollection.document(apartment.id.toString()).set(apartment).await()
            true
        } catch (e: Exception) {
            println("Save error: ${e.message}")
            false
        }
    }


    suspend fun deleteApartment(apartmentId: String): Boolean {
        return try {
            listingsCollection.document(apartmentId).delete().await()
            true
        } catch (e: Exception) {
            println("Delete error: ${e.message}")
            false
        }
    }
}
