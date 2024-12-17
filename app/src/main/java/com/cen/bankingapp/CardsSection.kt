// CardsSection.kt
package com.cen.bankingapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cen.bankingapp.data.Card
import com.cen.bankingapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun CardsSection(onAddCard: () -> Unit) {
    var cards by remember { mutableStateOf(listOf<Card>()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("Users").child(userId).child("cards")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cardList = mutableListOf<Card>()
                    for (cardSnapshot in snapshot.children) {
                        val card = cardSnapshot.getValue(Card::class.java)
                        if (card != null) {
                            cardList.add(card)
                        }
                    }
                    cards = cardList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    LazyRow {
        items(cards.size) { index ->
            CardItem(
                card = cards[index],
                onDeleteCard = { card -> deleteCard(userId, card) },
                onUpdateCard = { card -> updateCard(userId, card) }
            )
        }
        item {
            AddCardItem(onAddCard = onAddCard)
        }
    }
}

fun deleteCard(userId: String, card: Card) {
    val database = FirebaseDatabase.getInstance().reference
    database.child("Users").child(userId).child("cards").child(card.cardid).removeValue()
}

fun updateCard(userId: String, card: Card) {
    val database = FirebaseDatabase.getInstance().reference
    database.child("Users").child(userId).child("cards").child(card.cardid).setValue(card)
}

@Composable
fun CardItem(card: Card, onDeleteCard: (Card) -> Unit, onUpdateCard: (Card) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val image = if (card.cardType == "MASTERCARD") {
        painterResource(id = R.drawable.ic_mastercard)
    } else {
        painterResource(id = R.drawable.ic_visa)
    }

    val cardColor = if (card.cardType == "MASTER CARD") {
        Brush.horizontalGradient(colors = listOf(Color(0xFFf79e1b), Color(0xFFff5f00)))
    } else {
        Brush.horizontalGradient(colors = listOf(Color(0xFF1a1f71), Color(0xFFff5f00)))
    }

    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showDialog = true }
                )
            }
            .clickable { showDialog = true }
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(cardColor)
                .width(250.dp)
                .height(160.dp)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = image,
                contentDescription = card.cardName,
                modifier = Modifier
                    .width(60.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = card.cardName,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$ ${card.balance}",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = card.cardNumber,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(dismissOnClickOutside = true)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Choose an action", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        showUpdateDialog = true
                        showDialog = false
                    }) {
                        Text("Update Card")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        onDeleteCard(card)
                        showDialog = false
                    }) {
                        Text("Delete Card")
                    }
                }
            }
        }
    }

    if (showUpdateDialog) {
        UpdateCardDialog(
            card = card,
            onDismiss = { showUpdateDialog = false },
            onUpdate = { cardName, cardNumber, cardType ->
                val updatedCard = card.copy(cardName = cardName, cardNumber = cardNumber, cardType = cardType)
                onUpdateCard(updatedCard)
            }
        )
    }
}

@Composable
fun AddCardItem(onAddCard: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onAddCard() }
            .width(250.dp)
            .height(160.dp)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Card",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add a Card",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCardDialog(
    card: Card,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String) -> Unit
) {
    var cardName by remember { mutableStateOf(card.cardName) }
    var cardNumber by remember { mutableStateOf(card.cardNumber) }
    var cardType by remember { mutableStateOf(card.cardType) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Update Card", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                Button(onClick = {
                    onUpdate(cardName, cardNumber, cardType)
                    onDismiss()
                }) {
                    Text("Update")
                }
            }
        }
    }
}