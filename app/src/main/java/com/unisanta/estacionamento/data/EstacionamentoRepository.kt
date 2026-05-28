package com.unisanta.estacionamento.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class EstacionamentoRepository {

    private val db = FirebaseFirestore.getInstance()

    private val clientes = db.collection("clientes")
    private val carros = db.collection("carros")
    private val configRef = db.collection("estacionamento").document("config")

    // ---------------------------------------------------------------------
    // CLIENTES
    // ---------------------------------------------------------------------

    suspend fun criarClienteParaOperador(uid: String, nome: String) {
        val dados = hashMapOf(
            "nome" to nome,
            "telefone" to "",
            "createdAt" to Timestamp.now()
        )
        clientes.document(uid).set(dados).await()
    }
    suspend fun adicionarCliente(nome: String, telefone: String) {
        val dados = hashMapOf(
            "nome" to nome,
            "telefone" to telefone,
            "createdAt" to Timestamp.now()
        )
        clientes.add(dados).await()
    }

    suspend fun listarClientes(): List<Cliente> {
        val snapshot = clientes.orderBy("nome").get().await()
        return snapshot.toObjects(Cliente::class.java)
    }

    // ---------------------------------------------------------------------
    // ESTACIONAMENTO (config)
    // ---------------------------------------------------------------------
    suspend fun getConfig(): Estacionamento {
        val doc = configRef.get().await()
        return if (doc.exists()) {
            doc.toObject(Estacionamento::class.java) ?: Estacionamento()
        } else {
            val padrao = Estacionamento()
            configRef.set(padrao).await()
            padrao
        }
    }

    // ---------------------------------------------------------------------
    // CARROS
    // ---------------------------------------------------------------------
    suspend fun adicionarCarro(carro: Carro) {
        val jaExiste = buscarCarro(carro.placa)
        if (jaExiste != null) {
            throw Exception("Ja existe um carro com a placa ${carro.placa}")
        }
        val dados = hashMapOf(
            "placa" to carro.placa,
            "modelo" to carro.modelo,
            "cor" to carro.cor,
            "clienteId" to carro.clienteId,
            "vagaAtual" to null,
            "horaEntrada" to null,
            "horaSaida" to null,
            "status" to "FORA"
        )
        carros.add(dados).await()
    }

    suspend fun buscarCarro(placa: String): Carro? {
        val snapshot = carros.whereEqualTo("placa", placa).get().await()
        return snapshot.documents.firstOrNull()?.toObject(Carro::class.java)
    }

    suspend fun listarTodosCarros(): List<Carro> {
        return carros.get().await().toObjects(Carro::class.java)
    }

    suspend fun entrarNoEstacionamento(carroId: String, vaga: Int, placa: String) {
        val batch = db.batch()
        val carroRef = carros.document(carroId)

        batch.update(
            carroRef, mapOf(
                "vagaAtual" to vaga,
                "horaEntrada" to Timestamp.now(),
                "horaSaida" to null,
                "status" to "ESTACIONADO"
            )
        )
        batch.update(configRef, "vagasOcupadas.$vaga", placa)

        batch.commit().await()
    }
//-------------------------------------------------------------------------------------------------------------------------------
    suspend fun sairDoEstacionamento(carroId: String) {
        val carro = carros.document(carroId).get().await().toObject(Carro::class.java)
        val vaga = carro?.vagaAtual

        val batch = db.batch()
        val carroRef = carros.document(carroId)

        batch.update(
            carroRef, mapOf(
                "horaSaida" to Timestamp.now(),
                "vagaAtual" to null,
                "status" to "FORA"
            )
        )
        if (vaga != null) {
            batch.update(configRef, "vagasOcupadas.$vaga", FieldValue.delete())
        }

        batch.commit().await()
    }

    suspend fun removerCarro(placa: String) {
        val carro = buscarCarro(placa) ?: return
        sairDoEstacionamento(carro.id)
    }

    fun observarCarrosEstacionados(callback: (List<Carro>) -> Unit): ListenerRegistration {
        return carros
            .whereEqualTo("status", "ESTACIONADO")
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.toObjects(Carro::class.java) ?: emptyList()
                callback(lista.sortedBy { it.vagaAtual ?: 0 })
            }
    }
}
