package com.cen.bankingapp.data

data class Card(
    val cardid: String = "",
    val cardType: String = "",
    val cardNumber: String = "",
    val cardName: String = "",
    val balance: Double = 0.0
) {
    // No-argument constructor required for Firebase
//    constructor() : this("", "", "", 0.0)
}