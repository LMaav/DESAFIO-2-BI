package com.unisanta.estacionamento.data

data class Estacionamento(
    val totalVagas: Int = 20,
    val tarifaPorHora: Double = 10.0,
    val vagasOcupadas: Map<String, String> = emptyMap()
) {
    fun verificarDisponibilidade(): Int {
        return totalVagas - vagasOcupadas.size
    }
}
