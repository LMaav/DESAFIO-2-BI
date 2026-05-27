package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.unisanta.estacionamento.databinding.ActivityLoginBinding

// RF-01: Login com e-mail e senha via Firebase Auth.
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntrar.setOnClickListener { fazerLogin() }

        binding.btnIrCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    private fun fazerLogin() {
        val email = binding.editEmail.text.toString().trim()
        val senha = binding.editSenha.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                binding.progress.visibility = View.GONE
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { erro ->
                binding.progress.visibility = View.GONE
                Toast.makeText(this, "Erro ao entrar: ${erro.message}", Toast.LENGTH_LONG).show()
            }
    }
}
