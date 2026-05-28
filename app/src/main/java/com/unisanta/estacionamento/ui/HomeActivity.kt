package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.unisanta.estacionamento.data.Carro
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val repo = EstacionamentoRepository()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGerenciarVagas.setOnClickListener {
            startActivity(Intent(this, GerenciarVagasActivity::class.java))
        }
        binding.btnCadastrarCarro.setOnClickListener {
            startActivity(Intent(this, CadastrarCarroActivity::class.java))
        }
        binding.btnClientes.setOnClickListener {
            startActivity(Intent(this, ClientesActivity::class.java))
        }
        binding.btnSair.setOnClickListener { logout() }
    }

    override fun onStart() {
        super.onStart()
        carregarResumo()
        ouvirCarrosEstacionados()
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
    }

    private fun carregarResumo() {
        lifecycleScope.launch {
            try {
                val config = repo.getConfig()
                binding.txtTotalVagas.text = "Total: ${config.totalVagas}"
                binding.txtVagasLivres.text = "Livres: ${config.verificarDisponibilidade()}"
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Erro ao carregar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun ouvirCarrosEstacionados() {
        listener = repo.observarCarrosEstacionados { lista ->
            mostrarLista(lista)
            carregarResumo()
        }
    }

    private fun mostrarLista(carros: List<Carro>) {
        val textos = if (carros.isEmpty()) {
            listOf("Nenhum carro estacionado")
        } else {
            carros.map { "Vaga ${it.vagaAtual} - ${it.placa} (${it.modelo})" }
        }
        binding.listaCarros.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, textos)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}
