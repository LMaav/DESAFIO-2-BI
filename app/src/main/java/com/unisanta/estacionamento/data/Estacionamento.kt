package com.unisanta.estacionamento.data

// Representa o documento unico "config" da colecao "estacionamento".
data class Estacionamento(
    val totalVagas: Int = 20,
    val tarifaPorHora: Double = 10.0,
    // Mapa: numero da vaga -> placa do carro que esta nela.
    // No Firestore as chaves de Map sao String, por isso usamos String aqui.
    val vagasOcupadas: Map<String, String> = emptyMap()
) {
    // RF-14: quantas vagas ainda estao livres.
    fun verificarDisponibilidade(): Int {
        return totalVagas - vagasOcupadas.size
    }
}
