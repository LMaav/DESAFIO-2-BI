package com.unisanta.estacionamento.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unisanta.estacionamento.data.Carro
import com.unisanta.estacionamento.data.Cliente
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityCadastrarCarroBinding
import kotlinx.coroutines.launch

class CadastrarCarroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastrarCarroBinding
    private val repo = EstacionamentoRepository()
    private var clientes: List<Cliente> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastrarCarroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarClientes()
        binding.btnSalvar.setOnClickListener { salvar() }
    }

    // Carrega os clientes para preencher o Spinner de selecao.
    private fun carregarClientes() {
        lifecycleScope.launch {
            try {
                clientes = repo.listarClientes()
                val nomes = clientes.map { it.nome }
                binding.spinnerClientes.adapter =
                    ArrayAdapter(this@CadastrarCarroActivity, android.R.layout.simple_spinner_dropdown_item, nomes)
            } catch (e: Exception) {
                Toast.makeText(this@CadastrarCarroActivity, "Erro ao carregar clientes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun salvar() {
        val placa = binding.editPlaca.text.toString().trim().uppercase()
        val modelo = binding.editModelo.text.toString().trim()
        val cor = binding.editCor.text.toString().trim()

        if (placa.isEmpty() || modelo.isEmpty() || cor.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (clientes.isEmpty()) {
            Toast.makeText(this, "Cadastre um cliente antes", Toast.LENGTH_SHORT).show()
            return
        }

        val clienteSelecionado = clientes[binding.spinnerClientes.selectedItemPosition]

        val carro = Carro(
            placa = placa,
            modelo = modelo,
            cor = cor,
            clienteId = clienteSelecionado.id
        )

        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                repo.adicionarCarro(carro) // valida placa duplicada por dentro
                binding.progress.visibility = View.GONE
                Toast.makeText(this@CadastrarCarroActivity, "Carro cadastrado!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                binding.progress.visibility = View.GONE
                Toast.makeText(this@CadastrarCarroActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
