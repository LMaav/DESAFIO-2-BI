package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.unisanta.estacionamento.data.EstacionamentoRepository
import com.unisanta.estacionamento.databinding.ActivityCadastroBinding
import kotlinx.coroutines.launch

// RF-02: cria conta no Firebase Auth e o documento do cliente no Firestore.
class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private val auth = FirebaseAuth.getInstance()
    private val repo = EstacionamentoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCadastrar.setOnClickListener { cadastrar() }
    }

    private fun cadastrar() {
        val nome = binding.editNome.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val senha = binding.editSenha.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid ?: ""
                // Cria o documento do cliente usando o UID gerado pelo Firebase.
                lifecycleScope.launch {
                    try {
                        repo.criarClienteParaOperador(uid, nome)
                        binding.progress.visibility = View.GONE
                        Toast.makeText(this@CadastroActivity, "Conta criada!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CadastroActivity, HomeActivity::class.java))
                        finishAffinity()
                    } catch (e: Exception) {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(this@CadastroActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener { erro ->
                binding.progress.visibility = View.GONE
                Toast.makeText(this, "Erro ao cadastrar: ${erro.message}", Toast.LENGTH_LONG).show()
            }
    }
}
