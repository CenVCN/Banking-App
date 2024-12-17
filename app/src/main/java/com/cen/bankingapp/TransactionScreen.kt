package com.cen.bankingapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cen.bankingapp.data.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController) {
    val transactions = remember { mutableStateOf(listOf<Transaction>()) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("Users").child(userId).child("transactions").get()
                .addOnSuccessListener { snapshot ->
                    val transactionList = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                    transactions.value = transactionList
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(transactions.value) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBBDEFB) // Light blue background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Type: ${transaction.type}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1976D2) // Darker blue text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Amount: ${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF0D47A1) // Even darker blue text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: ${DateFormat.getDateTimeInstance().format(Date(transaction.timestamp))}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0D47A1) // Even darker blue text
            )
        }
    }
}