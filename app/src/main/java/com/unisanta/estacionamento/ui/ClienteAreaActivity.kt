package com.unisanta.estacionamento.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityClienteAreaBinding
import kotlinx.coroutines.launch
import kotlin.math.ceil

class ClienteAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteAreaBinding
    private val repo = EstacionamentoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConsultar.setOnClickListener { consultar() }
    }

    private fun consultar() {
        val placa = binding.editPlaca.text.toString().trim().uppercase()
        if (placa.isEmpty()) {
            Toast.makeText(this, "Digite a placa", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val carro = repo.buscarCarro(placa)
                if (carro == null) {
                    binding.txtResultado.text = "Nenhum carro encontrado com a placa $placa."
                    return@launch
                }

                val config = repo.getConfig()
                val texto = StringBuilder()
                texto.appendLine("Placa: ${carro.placa}")
                texto.appendLine("Modelo: ${carro.modelo}")
                texto.appendLine("Cor: ${carro.cor}")

                if (carro.status == "ESTACIONADO") {
                    texto.appendLine("Situacao: Estacionado na vaga ${carro.vagaAtual}")
                    val entrada = carro.horaEntrada
                    if (entrada != null) {
                        // Estimativa ate agora: (agora - entrada) em horas (arredondado pra cima) x tarifa.
                        val segundos = Timestamp.now().seconds - entrada.seconds
                        val horas = ceil(segundos / 3600.0)
                        val estimado = horas * config.tarifaPorHora
                        texto.appendLine("Valor estimado ate agora: R$ %.2f".format(estimado))
                    }
                } else {
                    texto.appendLine("Situacao: Fora do estacionamento")
                    if (carro.horaEntrada != null && carro.horaSaida != null) {
                        val custo = carro.calcularCustoEstacionamento(config.tarifaPorHora)
                        texto.appendLine("Valor da ultima estadia: R$ %.2f".format(custo))
                    }
                }

                binding.txtResultado.text = texto.toString().trim()
            } catch (e: Exception) {
                Toast.makeText(this@ClienteAreaActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
