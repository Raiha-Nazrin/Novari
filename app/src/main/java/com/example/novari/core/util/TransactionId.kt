package com.example.novari.core.util

import java.util.UUID

object TransactionId {
    fun new(): String = UUID.randomUUID().toString()
}
