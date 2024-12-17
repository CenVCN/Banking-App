package com.cen.bankingapp

object Utils {
    fun calculateInterest(balance: Double, rate: Double = 0.05): Double {
        return balance * rate
    }
}