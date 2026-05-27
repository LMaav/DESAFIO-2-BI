package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// Tela inicial. So decide para onde mandar o usuario:
// - Se ja esta logado -> Home
// - Se nao esta logado -> Login
// (RF-04: autenticacao persistente)
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuarioLogado = FirebaseAuth.getInstance().currentUser

        val destino = if (usuarioLogado != null) {
            HomeActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destino))
        finish() // fecha a splash para nao voltar a ela
    }
}
