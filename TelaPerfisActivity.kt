package com.seu.meumouse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TelaPerfisActivity : AppCompatActivity() {

    private lateinit var gerenciador: GerenciadorPerfis
    private lateinit var adaptador: AdaptadorPerfis
    private lateinit var listaPerfis: RecyclerView
    private lateinit var switchDetecaoAuto: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_perfis)

        gerenciador = GerenciadorPerfis(this)
        inicializar()
        carregarLista()
    }

    private fun inicializar() {
        listaPerfis = findViewById(R.id.listaPerfis)
        switchDetecaoAuto = findViewById(R.id.switchDetecaoAuto)

        switchDetecaoAuto.isChecked = gerenciador.deteccaoAutomaticaAtiva
        switchDetecaoAuto.setOnCheckedChangeListener { _, ligado ->
            gerenciador.deteccaoAutomaticaAtiva = ligado
        }

        adaptador = AdaptadorPerfis(
            lista = mutableListOf(),
            aoClicar = { perfil ->
                gerenciador.aplicarPerfil(perfil)
                finish()
            },
            aoExcluir = { perfil ->
                gerenciador.excluirPerfil(perfil.id)
                carregarLista()
            }
        )

        listaPerfis.adapter = adaptador
        listaPerfis.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.botaoCriarPerfil).setOnClickListener {
            // Aqui você abre uma tela para digitar nome e pacote do jogo
            // Por enquanto, vamos criar um exemplo
            gerenciador.criarPerfil("Novo Perfil", "com.seu.jogo", mutableListOf())
            carregarLista()
        }

        findViewById<Button>(R.id.botaoVoltarPerfis).setOnClickListener { finish() }
    }

    private fun carregarLista() {
        adaptador.lista.clear()
        adaptador.lista.addAll(gerenciador.listarTodos())
        adaptador.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        carregarLista()
    }
}
