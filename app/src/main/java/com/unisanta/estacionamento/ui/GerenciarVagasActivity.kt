package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unisanta.estacionamento.R
import com.unisanta.estacionamento.data.Carro
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityGerenciarVagasBinding
import kotlinx.coroutines.launch

// RF-15: mostra todas as vagas (livres e ocupadas) e permite registrar entradas/ver detalhes.
class GerenciarVagasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGerenciarVagasBinding
    private val repo = EstacionamentoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGerenciarVagasBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        montarGrid()
    }

    private fun montarGrid() {
        lifecycleScope.launch {
            try {
                val config = repo.getConfig()
                binding.gridVagas.removeAllViews()

                for (vaga in 1..config.totalVagas) {
                    // vagasOcupadas usa a chave como String.
                    val placaNaVaga = config.vagasOcupadas[vaga.toString()]
                    val ocupada = placaNaVaga != null

                    val celula = TextView(this@GerenciarVagasActivity)
                    celula.text = if (ocupada) "Vaga $vaga\n$placaNaVaga" else "Vaga $vaga\nLivre"
                    celula.gravity = Gravity.CENTER
                    celula.setTextColor(resources.getColor(R.color.branco, theme))
                    celula.setBackgroundResource(
                        if (ocupada) R.drawable.vaga_ocupada else R.drawable.vaga_livre
                    )

                    // Define o tamanho da celula dentro do grid.
                    val params = GridLayout.LayoutParams()
                    params.width = 0
                    params.height = 220
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    params.setMargins(8, 8, 8, 8)
                    celula.layoutParams = params

                    celula.setOnClickListener {
                        if (ocupada) {
                            abrirDetalhe(placaNaVaga!!, vagaSugerida = -1)
                        } else {
                            escolherCarroParaVaga(vaga)
                        }
                    }

                    binding.gridVagas.addView(celula)
                }
            } catch (e: Exception) {
                Toast.makeText(this@GerenciarVagasActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Ao tocar numa vaga livre, escolhemos um carro que esta FORA para registrar a entrada.
    private fun escolherCarroParaVaga(vaga: Int) {
        lifecycleScope.launch {
            try {
                val fora = repo.listarTodosCarros().filter { it.status == "FORA" }
                if (fora.isEmpty()) {
                    Toast.makeText(this@GerenciarVagasActivity, "Nenhum carro disponivel. Cadastre um carro.", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val nomes = fora.map { "${it.placa} - ${it.modelo}" }.toTypedArray()
                AlertDialog.Builder(this@GerenciarVagasActivity)
                    .setTitle("Qual carro vai entrar na vaga $vaga?")
                    .setItems(nomes) { _, posicao ->
                        val escolhido: Carro = fora[posicao]
                        abrirDetalhe(escolhido.placa, vagaSugerida = vaga)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@GerenciarVagasActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun abrirDetalhe(placa: String, vagaSugerida: Int) {
        val intent = Intent(this, DetalhesCarroActivity::class.java)
        intent.putExtra("placa", placa)
        intent.putExtra("vagaSugerida", vagaSugerida)
        startActivity(intent)
    }
}
