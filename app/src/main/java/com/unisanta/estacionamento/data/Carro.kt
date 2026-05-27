package com.unisanta.estacionamento.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlin.math.ceil

// Representa um documento da colecao "carros".
data class Carro(
    @DocumentId val id: String = "",
    val placa: String = "",
    val modelo: String = "",
    val cor: String = "",
    val clienteId: String = "",
    val vagaAtual: Int? = null,
    val horaEntrada: Timestamp? = null,
    val horaSaida: Timestamp? = null,
    val status: String = "FORA" // "ESTACIONADO" ou "FORA"
) {

    // RF-08: calcula o custo = (horaSaida - horaEntrada) em horas x tarifa, arredondado para cima.
    fun calcularCustoEstacionamento(tarifaPorHora: Double): Double {
        val entrada = horaEntrada ?: return 0.0
        val saida = horaSaida ?: return 0.0

        val segundos = saida.seconds - entrada.seconds
        val horas = segundos / 3600.0
        val horasArredondadas = ceil(horas) // sempre arredonda para cima
        return horasArredondadas * tarifaPorHora
    }
}
