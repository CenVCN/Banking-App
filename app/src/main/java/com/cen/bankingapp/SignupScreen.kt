import android.media.Image
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.cen.bankingapp.CurrenciesSection
import com.cen.bankingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.cen.bankingapp.Utils
import com.cen.bankingapp.Utils.calculateInterest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(onSignupSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var signupError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    fun signupUser(email: String, password: String, name: String) {
        if (password != confirmPassword) {
            signupError = "Passwords do not match"
            return
        }

        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val database = FirebaseDatabase.getInstance().reference
                    val user = mapOf(
                        "userid" to userId,
                        "name" to name,
                        "email" to email,
                        "balance" to 0.0,
                        "interest" to calculateInterest(0.0),
                        "transactions" to emptyMap<String, Any>(),
                        "cards" to emptyMap<String, Any>()
                    )
                    database.child("Users").child(userId).setValue(user)
                        .addOnSuccessListener {
                            onSignupSuccess()
                        }
                        .addOnFailureListener {
                            signupError = "Failed to save user data"
                        }
                } else {
                    signupError = task.exception?.message
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes), // Replace with your drawable resource
            contentDescription = "Money Sign Logo",
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) R.drawable.password_eyes else R.drawable.password_eyes_closed
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painterResource(id = image), contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) R.drawable.password_eyes else R.drawable.password_eyes_closed
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(painterResource(id = image), contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                signupUser(email, password, name)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        if (signupError != null) {
            Text(
                text = signupError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen {
        // Handle signup logic here
    }
}