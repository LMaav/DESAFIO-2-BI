package com.unisanta.estacionamento.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Cliente(
    @DocumentId val id: String = "",
    val nome: String = "",
    val telefone: String = "",
    val createdAt: Timestamp? = null
)
