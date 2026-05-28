package com.unisanta.estacionamento.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unisanta.estacionamento.databinding.ActivityPerfilBinding


class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOperador.setOnClickListener {

            startActivity(Intent(this, SplashActivity::class.java))
        }
        binding.btnCliente.setOnClickListener {

            startActivity(Intent(this, ClienteAreaActivity::class.java))
        }
    }
}
