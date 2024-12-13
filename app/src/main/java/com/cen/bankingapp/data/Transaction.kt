package com.cen.bankingapp.data

data class Transaction(
    val id: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L
)