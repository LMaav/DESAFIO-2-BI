package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unisanta.estacionamento.data.Carro
import com.unisanta.estacionamento.data.Cliente
import com.unisanta.estacionamento.data.Estacionamento
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityDetalhesCarroBinding
import kotlinx.coroutines.launch

class DetalhesCarroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesCarroBinding
    private val repo = EstacionamentoRepository()

    private var placa: String = ""
    private var vagaSugerida: Int = -1

    private var carro: Carro? = null
    private var cliente: Cliente? = null
    private var config: Estacionamento = Estacionamento()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesCarroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        placa = intent.getStringExtra("placa") ?: ""
        vagaSugerida = intent.getIntExtra("vagaSugerida", -1)

        binding.btnEntrada.setOnClickListener { registrarEntrada() }
        binding.btnSaida.setOnClickListener { registrarSaida() }
        binding.btnCalcular.setOnClickListener { calcularCusto() }
    }

    override fun onStart() {
        super.onStart()
        carregar()
    }

    private fun carregar() {
        lifecycleScope.launch {
            try {
                config = repo.getConfig()
                carro = repo.buscarCarro(placa)
                cliente = repo.listarClientes().find { it.id == carro?.clienteId }
                mostrarDados()
            } catch (e: Exception) {
                Toast.makeText(this@DetalhesCarroActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarDados() {
        val c = carro
        if (c == null) {
            binding.txtDados.text = "Carro nao encontrado."
            return
        }

        binding.txtDados.text = """
            Placa: ${c.placa}
            Modelo: ${c.modelo}
            Cor: ${c.cor}
            Cliente: ${cliente?.nome ?: "-"}
            Status: ${c.status}
            Vaga: ${c.vagaAtual ?: "-"}
        """.trimIndent()

        val estacionado = c.status == "ESTACIONADO"
        binding.btnEntrada.visibility = if (estacionado) View.GONE else View.VISIBLE
        binding.btnSaida.visibility = if (estacionado) View.VISIBLE else View.GONE
    }

    private fun registrarEntrada() {
        val c = carro ?: return
        if (vagaSugerida != -1) {
            efetuarEntrada(c, vagaSugerida)
        } else {
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            AlertDialog.Builder(this)
                .setTitle("Numero da vaga")
                .setView(input)
                .setPositiveButton("Confirmar") { _, _ ->
                    val vaga = input.text.toString().toIntOrNull()
                    if (vaga == null || vaga < 1 || vaga > config.totalVagas) {
                        Toast.makeText(this, "Vaga invalida", Toast.LENGTH_SHORT).show()
                    } else if (config.vagasOcupadas.containsKey(vaga.toString())) {
                        Toast.makeText(this, "Essa vaga ja esta ocupada", Toast.LENGTH_SHORT).show()
                    } else {
                        efetuarEntrada(c, vaga)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun efetuarEntrada(c: Carro, vaga: Int) {
        lifecycleScope.launch {
            try {
                repo.entrarNoEstacionamento(c.id, vaga, c.placa)
                Toast.makeText(this@DetalhesCarroActivity, "Entrada registrada na vaga $vaga", Toast.LENGTH_SHORT).show()
                vagaSugerida = -1
                carregar()
            } catch (e: Exception) {
                Toast.makeText(this@DetalhesCarroActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registrarSaida() {
        val c = carro ?: return
        lifecycleScope.launch {
            try {
                repo.sairDoEstacionamento(c.id)
                Toast.makeText(this@DetalhesCarroActivity, "Saida registrada", Toast.LENGTH_SHORT).show()
                carregar()
            } catch (e: Exception) {
                Toast.makeText(this@DetalhesCarroActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun calcularCusto() {
        val c = carro ?: return
        if (c.horaEntrada == null || c.horaSaida == null) {
            Toast.makeText(this, "Registre entrada e saida primeiro", Toast.LENGTH_SHORT).show()
            return
        }
        val custo = c.calcularCustoEstacionamento(config.tarifaPorHora)
        binding.txtCusto.text = "Custo: R$ %.2f".format(custo)

    }
}
