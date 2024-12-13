// AddCardScreen.kt
package com.cen.bankingapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(navController: NavController, onCardAdded: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    var cardType by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var addCardError by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    fun addCardToUser(userId: String, cardType: String, cardNumber: String, cardName: String, balance: Double) {
        val database = FirebaseDatabase.getInstance().reference
        val cardId = database.child("Users").child(userId).child("cards").push().key ?: return
        val card = com.cen.bankingapp.data.Card(
            cardid = cardId,
            cardType = cardType,
            cardNumber = cardNumber,
            cardName = cardName,
            balance = balance
        )
        database.child("Users").child(userId).child("cards").child(cardId).setValue(card)
            .addOnSuccessListener {
                onCardAdded()
            }
            .addOnFailureListener {
                addCardError = "Failed to add card"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = cardType,
                onValueChange = { cardType = it },
                label = { Text("Card Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("VISA") },
                    onClick = {
                        cardType = "VISA"
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("MASTERCARD") },
                    onClick = {
                        cardType = "MASTERCARD"
                        expanded = false
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = cardName,
            onValueChange = { cardName = it },
            label = { Text("Card Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = balance,
            onValueChange = { balance = it },
            label = { Text("Balance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val balanceValue = balance.toDoubleOrNull()
                if (balanceValue != null) {
                    addCardToUser(userId, cardType, cardNumber, cardName, balanceValue)
                } else {
                    addCardError = "Invalid balance"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Card")
        }
        if (addCardError != null) {
            Text(
                text = addCardError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddCardScreenPreview() {
    AddCardScreen(navController = NavController(LocalContext.current)) {
        // Handle card added logic here
    }
}