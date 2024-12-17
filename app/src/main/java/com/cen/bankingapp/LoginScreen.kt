package com.cen.bankingapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onSignupRedirect: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }

    fun validateEmail(email: String): String? {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            null
        } else {
            "Invalid email address"
        }
    }

    fun validatePassword(password: String): String? {
        return if (password.isNotEmpty()) {
            null
        } else {
            "Password cannot be empty"
        }
    }

    fun loginUser(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val jwtToken = task.result?.user?.uid
                    onLogin(email, jwtToken ?: "")
                } else {
                    loginError = task.exception?.message
                }
            }
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val imageRes = if (isSystemInDarkTheme) {
        R.drawable.money_sign_light
    } else {
        R.drawable.money_sign_dark
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes), // Replace with your drawable resource
            contentDescription = "Money Sign Logo",
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )

//        Spacer(modifier = Modifier.height(0.0005.dp))

        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = validateEmail(it)
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null
        )
        if (emailError != null) {
            Text(
                text = emailError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it)
            },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null
        )
        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                emailError = validateEmail(email)
                passwordError = validatePassword(password)
                if (emailError == null && passwordError == null) {
                    loginUser(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSignupRedirect()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        if (loginError != null) {
            Text(
                text = loginError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLogin = { _, _ ->
            // Handle login logic here
        },
        onSignupRedirect = {
            // Handle signup redirect here
        }
    )
}