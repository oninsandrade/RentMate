package com.example.rentmate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            // ✅ LINKED: This clears the "Unused variable" warning
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(32.dp))

                SignUpInput(username, { username = it }, "Username")
                Spacer(modifier = Modifier.height(16.dp))
                SignUpInput(email, { email = it }, "Email", KeyboardType.Email)
                Spacer(modifier = Modifier.height(16.dp))
                SignUpPasswordInput(password, { password = it }, "Password")
                Spacer(modifier = Modifier.height(16.dp))
                SignUpPasswordInput(confirmPassword, { confirmPassword = it }, "Confirm Password")

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 🚀 FIREBASE SIGN UP LOGIC ---
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                        } else if (username.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill all fields"
                        } else {
                            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: ""
                                        val userData = hashMapOf(
                                            "username" to username,
                                            "email" to email,
                                            "userId" to userId,
                                            "createdAt" to com.google.firebase.Timestamp.now()
                                        )

                                        db.collection("users").document(userId).set(userData)
                                            .addOnSuccessListener {
                                                coroutineScope.launch {
                                                    // ✅ FEEDBACK: User now knows they succeeded
                                                    snackbarHostState.showSnackbar("Welcome to RentMate, $username!")
                                                    delay(1500)
                                                    navController.navigate("listings") {
                                                        popUpTo("welcome") { inclusive = true }
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Database error: ${e.message}"
                                            }
                                    } else {
                                        errorMessage = task.exception?.message ?: "Sign up failed"
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Sign Up", fontSize = 18.sp, color = Color.White)
                }

                // --- 🔘 GOOGLE SIGN UP SECTION ---
                Spacer(modifier = Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.5f))
                    Text(" Or sign up via ", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* Google Auth Logic */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SignUpInput(value: String, onValueChange: (String) -> Unit, label: String, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = type),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
fun SignUpPasswordInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}
