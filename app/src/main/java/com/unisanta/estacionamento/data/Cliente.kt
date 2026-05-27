package com.unisanta.estacionamento.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Representa um documento da colecao "clientes".
// Os valores padrao sao obrigatorios para o Firestore conseguir criar o objeto.
data class Cliente(
    @DocumentId val id: String = "",
    val nome: String = "",
    val telefone: String = "",
    val createdAt: Timestamp? = null
)
