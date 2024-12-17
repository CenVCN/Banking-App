package com.cen.bankingapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import com.cen.bankingapp.data.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.cen.bankingapp.CurrenciesSection
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

class WalletScreen {
    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun getBalance(onResult: (Double) -> Unit) {
        if (userId != null) {
            database.child("Users").child(userId).child("balance").get().addOnSuccessListener {
                val balance = it.getValue(Double::class.java) ?: 0.0
                onResult(balance)
            }
        }
    }

    fun getTransactions(onResult: (List<Transaction>) -> Unit) {
        if (userId != null) {
            database.child("Users").child(userId).child("transactions").get().addOnSuccessListener { snapshot ->
                val transactions = snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
                onResult(transactions)
            }
        }
    }


    fun addBalance(amount: Double, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (userId != null) {
            database.child("Users").child(userId).child("balance").get()
                .addOnSuccessListener { snapshot ->
                    val currentBalance = snapshot.getValue(Double::class.java) ?: 0.0
                    val newBalance = currentBalance + amount
                    database.child("Users").child(userId).child("balance").setValue(newBalance)
                        .addOnSuccessListener {
                            recordTransaction("add", amount)
                            onSuccess()
                        }
                        .addOnFailureListener { onFailure(it) }
                }.addOnFailureListener { onFailure(it) }
        }
    }

    fun withdrawBalance(
        cardName: String,
        cardNumber: String,
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (userId != null) {
            val cardRef = database.child("Users").child(userId).child("cards")
            cardRef.orderByChild("cardNumber").equalTo(cardNumber).get()
                .addOnSuccessListener { snapshot ->
                    val card = snapshot.children.firstOrNull { it.child("cardName").value == cardName }
                    if (card != null) {
                        val currentCardBalance = card.child("balance").getValue(Double::class.java) ?: 0.0
                        if (currentCardBalance >= amount) {
                            val newCardBalance = currentCardBalance - amount
                            cardRef.child(card.key!!).child("balance").setValue(newCardBalance)
                                .addOnSuccessListener {
                                    // Update user's balance
                                    database.child("Users").child(userId).child("balance").get()
                                        .addOnSuccessListener { userSnapshot ->
                                            val currentUserBalance = userSnapshot.getValue(Double::class.java) ?: 0.0
                                            val newUserBalance = currentUserBalance + amount
                                            database.child("Users").child(userId).child("balance").setValue(newUserBalance)
                                                .addOnSuccessListener {
                                                    recordTransaction("withdraw", amount)
                                                    onSuccess()
                                                }
                                                .addOnFailureListener { onFailure(it) }
                                        }
                                        .addOnFailureListener { onFailure(it) }
                                }
                                .addOnFailureListener { onFailure(it) }
                        } else {
                            onFailure(Exception("Insufficient card balance"))
                        }
                    } else {
                        onFailure(Exception("Card not found"))
                    }
                }
                .addOnFailureListener { onFailure(it) }
        }
    }

    fun sendBalance(amount: Double, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (userId != null) {
            database.child("Users").child(userId).child("balance").get()
                .addOnSuccessListener { snapshot ->
                    val currentBalance = snapshot.getValue(Double::class.java) ?: 0.0
                    if (currentBalance >= amount) {
                        val newBalance = currentBalance - amount
                        database.child("Users").child(userId).child("balance").setValue(newBalance)
                            .addOnSuccessListener {
                                recordTransaction("send", amount)
                                onSuccess()
                            }
                            .addOnFailureListener { onFailure(it) }
                    } else {
                        onFailure(Exception("Insufficient balance"))
                    }
                }.addOnFailureListener { onFailure(it) }
        }
    }

    fun recordTransaction(type: String, amount: Double) {
        if (userId != null) {
            val transactionId = database.child("Users").child(userId).child("transactions").push().key
            if (transactionId != null) {
                val transaction = Transaction(
                    id = transactionId,
                    type = type,
                    amount = amount,
                    timestamp = System.currentTimeMillis()
                )
                database.child("Users").child(userId).child("transactions").child(transactionId).setValue(transaction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onSendBalance: () -> Unit,
    onAddBalance: () -> Unit,
    onWithdrawBalance: () -> Unit,
    navController: NavController
) {
    val walletScreen = remember { WalletScreen() }
    var balance by remember { mutableStateOf(0.0) }
    var showSendDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var contactNumber by remember { mutableStateOf(TextFieldValue("")) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var cardName by remember { mutableStateOf(TextFieldValue("")) }
    var cardNumber by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        walletScreen.getBalance {
            balance = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("homeScreen") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Account Balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1976D2) // Darker blue text
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "$$balance",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFF0D47A1) // Even darker blue text
                            )
                        }
                    }
                    Button(
                        onClick = { showSendDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2), // Darker blue background
                            contentColor = Color.White // White text
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Send Balance", style = MaterialTheme.typography.bodyLarge)
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2), // Darker blue background
                            contentColor = Color.White // White text
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Balance", style = MaterialTheme.typography.bodyLarge)
                    }
                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2), // Darker blue background
                            contentColor = Color.White // White text
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Withdraw Balance", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CurrenciesSection()
            }
        }
    }

    if (showSendDialog) {
        Dialog(onDismissRequest = { showSendDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Send Balance", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            amountError = null
                        },
                        label = { Text("Amount") },
                        isError = amountError != null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (amountError != null) {
                        Text(
                            text = amountError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.text.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                amountError = "Please enter a valid amount"
                            } else if (amountValue > balance) {
                                amountError = "Insufficient balance"
                            } else {
                                walletScreen.sendBalance(amountValue, {
                                    balance -= amountValue
                                    showSendDialog = false
                                }, {
                                    // Handle error
                                })
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Add Balance", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            amountError = null
                        },
                        label = { Text("Amount") },
                        isError = amountError != null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (amountError != null) {
                        Text(
                            text = amountError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.text.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                amountError = "Please enter a valid amount"
                            } else {
                                walletScreen.addBalance(amountValue, {
                                    balance += amountValue
                                    showAddDialog = false
                                }, {
                                    // Handle error
                                })
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        Dialog(onDismissRequest = { showWithdrawDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Withdraw Balance", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Card Name") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            amountError = null
                        },
                        label = { Text("Amount") },
                        isError = amountError != null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (amountError != null) {
                        Text(
                            text = amountError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.text.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                amountError = "Please enter a valid amount"
                            } else {
                                walletScreen.withdrawBalance(
                                    cardName.text,
                                    cardNumber.text,
                                    amountValue,
                                    {
                                        balance -= amountValue
                                        showWithdrawDialog = false
                                    },
                                    {
                                        // Handle error
                                    })
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Withdraw")
                    }

                }
            }
        }
    }
    @Composable
    fun CurrencyItem(currency: String, amount: Double) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD) // Light blue background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1976D2) // Darker blue text
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$$amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0D47A1) // Even darker blue text
                )
            }
        }
    }
}
