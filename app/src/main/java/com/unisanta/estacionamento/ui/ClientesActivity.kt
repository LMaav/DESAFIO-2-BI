package com.unisanta.estacionamento.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityClientesBinding
import kotlinx.coroutines.launch


class ClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientesBinding
    private val repo = EstacionamentoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdicionar.setOnClickListener { dialogAdicionar() }
    }

    override fun onStart() {
        super.onStart()
        carregar()
    }

    private fun carregar() {
        lifecycleScope.launch {
            try {
                val clientes = repo.listarClientes()
                val textos = if (clientes.isEmpty()) {
                    listOf("Nenhum cliente cadastrado")
                } else {
                    clientes.map { "${it.nome}  ${it.telefone}" }
                }
                binding.listaClientes.adapter =
                    ArrayAdapter(this@ClientesActivity, android.R.layout.simple_list_item_1, textos)
            } catch (e: Exception) {
                Toast.makeText(this@ClientesActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun dialogAdicionar() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 24, 48, 0)

        val editNome = EditText(this)
        editNome.hint = "Nome"
        val editTelefone = EditText(this)
        editTelefone.hint = "Telefone"

        layout.addView(editNome)
        layout.addView(editTelefone)

        AlertDialog.Builder(this)
            .setTitle("Novo cliente")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = editNome.text.toString().trim()
                val telefone = editTelefone.text.toString().trim()
                if (nome.isEmpty()) {
                    Toast.makeText(this, "Informe o nome", Toast.LENGTH_SHORT).show()
                } else {
                    salvarCliente(nome, telefone)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarCliente(nome: String, telefone: String) {
        lifecycleScope.launch {
            try {
                repo.adicionarCliente(nome, telefone)
                Toast.makeText(this@ClientesActivity, "Cliente adicionado!", Toast.LENGTH_SHORT).show()
                carregar()
            } catch (e: Exception) {
                Toast.makeText(this@ClientesActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
