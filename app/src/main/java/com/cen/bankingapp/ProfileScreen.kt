package com.cen.bankingapp

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference
    val userId = auth.currentUser?.uid
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            database.child("Users").child(userId).get().addOnSuccessListener { snapshot ->
                name = snapshot.child("name").getValue(String::class.java) ?: ""
                email = snapshot.child("email").getValue(String::class.java) ?: ""
            }
        }
    }

    fun updateProfile(newName: String, newEmail: String, newPassword: String) {
        if (userId != null) {
            loading = true
            val user = auth.currentUser
            val profileUpdates = userProfileChangeRequest {
                displayName = newName
            }
            user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updateEmail(newEmail).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            user.sendEmailVerification().addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    if (newPassword.isNotEmpty()) {
                                        user.updatePassword(newPassword).addOnCompleteListener { passwordTask ->
                                            if (passwordTask.isSuccessful) {
                                                val updates = hashMapOf<String, Any>(
                                                    "name" to newName,
                                                    "email" to newEmail
                                                )
                                                database.child("Users").child(userId).updateChildren(updates)
                                                    .addOnSuccessListener {
                                                        loading = false
                                                        showUpdateDialog = false
                                                    }
                                                    .addOnFailureListener {
                                                        error = it.message
                                                        loading = false
                                                    }
                                            } else {
                                                error = passwordTask.exception?.message
                                                loading = false
                                            }
                                        }
                                    } else {
                                        val updates = hashMapOf<String, Any>(
                                            "name" to newName,
                                            "email" to newEmail
                                        )
                                        database.child("Users").child(userId).updateChildren(updates)
                                            .addOnSuccessListener {
                                                loading = false
                                                showUpdateDialog = false
                                            }
                                            .addOnFailureListener {
                                                error = it.message
                                                loading = false
                                            }
                                    }
                                } else {
                                    error = verificationTask.exception?.message
                                    loading = false
                                }
                            }
                        } else {
                            error = emailTask.exception?.message
                            loading = false
                        }
                    }
                } else {
                    error = task.exception?.message
                    loading = false
                }
            }
        }
    }

    fun deleteProfile(navController: NavController) {
        if (userId != null) {
            database.child("Users").child(userId).removeValue()
                .addOnSuccessListener {
                    auth.currentUser?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController.navigate("loginScreen") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        } else {
                            // Handle error
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
        }
    }

    fun logout() {
        loading = true
        auth.signOut()
        navController.navigate("loginScreen") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Name: $name", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Email: $email", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2), // Darker blue background
                        contentColor = Color.White // White text
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update Profile", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F), // Red background
                        contentColor = Color.White // White text
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Profile", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2), // Darker blue background
                        contentColor = Color.White // White text
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Logout", style = MaterialTheme.typography.bodyLarge)
                }
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (showUpdateDialog) {
            Dialog(onDismissRequest = { showUpdateDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Update Profile", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (leave blank to keep current)") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                updateProfile(name, email, password)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2), // Darker blue background
                                contentColor = Color.White // White text
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            Dialog(onDismissRequest = { showDeleteDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Are you sure you want to delete your profile?", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    showDeleteDialog = false
                                    deleteProfile(navController)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F), // Red background
                                    contentColor = Color.White // White text
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Yes", style = MaterialTheme.typography.bodyLarge)
                            }
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2), // Darker blue background
                                    contentColor = Color.White // White text
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("No", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}