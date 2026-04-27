package com.example.rentmate

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ApartmentViewModel // ✅ FIX 1: Resolved "Unresolved reference: viewModel"
) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val user = auth.currentUser

    // 🛡️ Get current values to clear "Assigned value is never read" warnings
    val originalName = user?.displayName ?: "New User"
    val originalEmail = user?.email ?: ""
    val originalPhone = user?.phoneNumber ?: ""

    var name by remember { mutableStateOf(originalName) }
    var email by remember { mutableStateOf(originalEmail) }
    var phone by remember { mutableStateOf(originalPhone) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDiscardDialog by remember { mutableStateOf(false) }

    // ✅ Logic: Button only enables if Nami actually changed something
    val hasChanges = name != originalName || email != originalEmail ||
            phone != originalPhone || selectedImageUri != null

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Leave anyway?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    navController.popBackStack()
                }) { Text("DISCARD", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("KEEP EDITING") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                    IconButton(onClick = {
                        if (hasChanges) showDiscardDialog = true else navController.popBackStack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 📸 PROFILE IMAGE ---
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(24.dp))
                        }
                    }
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                }

                Spacer(Modifier.height(32.dp))

                EditField("Full Name", name, { name = it }, Icons.Default.Person)
                Spacer(Modifier.height(16.dp))
                EditField("Email", email, { email = it }, Icons.Default.Email)
                Spacer(Modifier.height(16.dp))
                EditField("Phone Number", phone, { phone = it }, Icons.Default.Phone)

                Spacer(Modifier.weight(1f))


                Button(
                    onClick = {
                        // ✅ FIX 2: Correctly calls the ViewModel to update Firebase
                        viewModel.updateProfile(name, selectedImageUri) { success ->
                            coroutineScope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Profile Updated! ✅")
                                    delay(1000)
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("Update failed ❌")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = hasChanges, // Clears the "hasChanges is never used" warning
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}
