package com.example.rentmate

import AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(navController: NavController, authViewModel: AuthViewModel) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ⏳ Observe loading state from ViewModel
    val isLoading by authViewModel.isLoading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(bottom = 16.dp, start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Change Password", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PasswordField("Current Password", oldPassword) { oldPassword = it }
                PasswordField("New Password", newPassword) { newPassword = it }
                PasswordField("Confirm New Password", confirmPassword) { confirmPassword = it }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(Modifier.weight(1f))

                // 🚀 THE LOGIC: Connects to Firebase
                Button(
                    onClick = {
                        when {
                            newPassword != confirmPassword -> errorMessage = "Passwords do not match"
                            newPassword.length < 6 -> errorMessage = "Must be at least 6 characters"
                            else -> {
                                errorMessage = ""
                                // ✅ This is the actual Firebase call
                                authViewModel.changePassword(oldPassword, newPassword) { success, message ->
                                    if (success) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Password Updated! ✅")
                                            delay(1500)
                                            navController.popBackStack()
                                        }
                                    } else {
                                        errorMessage = message // "Wrong old password" etc.
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Update Password", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        // ✅ Polish: Toggle visibility to prevent typos
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            val icon = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(icon, null)
            }
        }
    )
}
