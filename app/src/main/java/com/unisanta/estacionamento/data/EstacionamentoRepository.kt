package com.unisanta.estacionamento.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

// Esta classe "abstrai" o Firestore: as telas chamam estes metodos e nao
// precisam saber como o banco funciona por dentro.
class EstacionamentoRepository {

    private val db = FirebaseFirestore.getInstance()

    private val clientes = db.collection("clientes")
    private val carros = db.collection("carros")
    // O estado global do estacionamento fica em um documento unico chamado "config".
    private val configRef = db.collection("estacionamento").document("config")

    // ---------------------------------------------------------------------
    // CLIENTES
    // ---------------------------------------------------------------------

    // RF-02: chamado no cadastro do operador. Cria o documento usando o UID do Auth.
    suspend fun criarClienteParaOperador(uid: String, nome: String) {
        val dados = hashMapOf(
            "nome" to nome,
            "telefone" to "",
            "createdAt" to Timestamp.now()
        )
        clientes.document(uid).set(dados).await()
    }

    // Adiciona um novo cliente (dono de carro) com id automatico.
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

    // Le a config; se ainda nao existir, cria uma com valores padrao.
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

    // RF-05 / RF-11: cadastra um carro, validando placa nao duplicada.
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

    // RF-13: busca um carro pela placa. Retorna o Carro ou null.
    suspend fun buscarCarro(placa: String): Carro? {
        val snapshot = carros.whereEqualTo("placa", placa).get().await()
        return snapshot.documents.firstOrNull()?.toObject(Carro::class.java)
    }

    suspend fun listarTodosCarros(): List<Carro> {
        return carros.get().await().toObjects(Carro::class.java)
    }

    // RF-06: registra a ENTRADA de um carro em uma vaga.
    // Usamos um batch para atualizar carro e config de forma atomica.
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
        // Marca a vaga como ocupada no mapa do estacionamento.
        batch.update(configRef, "vagasOcupadas.$vaga", placa)

        batch.commit().await()
    }

    // RF-07: registra a SAIDA do carro e libera a vaga.
    suspend fun sairDoEstacionamento(carroId: String) {
        // Primeiro descobrimos em qual vaga o carro esta.
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
        // Libera a vaga no mapa (se o carro estava em alguma vaga).
        if (vaga != null) {
            batch.update(configRef, "vagasOcupadas.$vaga", FieldValue.delete())
        }

        batch.commit().await()
    }

    // RF-12: remove o carro da lista ativa (faz a saida).
    suspend fun removerCarro(placa: String) {
        val carro = buscarCarro(placa) ?: return
        sairDoEstacionamento(carro.id)
    }

    // RF-10: observa em TEMPO REAL os carros que estao estacionados.
    // Sempre que algo mudar no Firestore, o callback e chamado com a lista nova.
    fun observarCarrosEstacionados(callback: (List<Carro>) -> Unit): ListenerRegistration {
        return carros
            .whereEqualTo("status", "ESTACIONADO")
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.toObjects(Carro::class.java) ?: emptyList()
                // Ordenamos pela vaga aqui no app (evita precisar criar indice no Firestore).
                callback(lista.sortedBy { it.vagaAtual ?: 0 })
            }
    }
}
