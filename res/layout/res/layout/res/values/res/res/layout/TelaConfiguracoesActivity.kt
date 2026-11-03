package com.seu.meumouse

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TelaConfiguracoesActivity : AppCompatActivity() {

    private lateinit var barraSensibilidade: SeekBar
    private lateinit var valorSensibilidade: TextView
    private lateinit var seletorPerfil: Spinner
    private lateinit var perfilAdapter: ArrayAdapter<String>
    private val listaPerfis = mutableListOf("PADRÃO", "FREE FIRE", "CALL OF DUTY")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_configuracoes)

        Config.init(this)
        inicializarComponentes()
        carregarValoresSalvos()
        configurarEventos()
    }

    private fun inicializarComponentes() {
        barraSensibilidade = findViewById(R.id.barraSensibilidade)
        valorSensibilidade = findViewById(R.id.valorSensibilidade)
        seletorPerfil = findViewById(R.id.seletorPerfil)

        perfilAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaPerfis)
        perfilAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seletorPerfil.adapter = perfilAdapter
    }

    private fun carregarValoresSalvos() {
        val sensibilidadeAtual = Config.sensibilidadeMouse
        barraSensibilidade.progress = (sensibilidadeAtual * 10).toInt()
        atualizarTextoSensibilidade(sensibilidadeAtual)

        val modoAtual = Config.modoMouse
        findViewById<RadioButton>(
            if (modoAtual == "CÂMERA") R.id.modoCamera else R.id.modoCursor
        ).isChecked = true

        val posicaoPerfil = listaPerfis.indexOf(Config.perfilAtual)
        if (posicaoPerfil >= 0) seletorPerfil.setSelection(posicaoPerfil)
    }

    private fun atualizarTextoSensibilidade(valor: Float) {
        valorSensibilidade.text = String.format("%.1fx", valor)
    }

    private fun configurarEventos() {
        barraSensibilidade.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val valor = (progress / 10.0f).coerceIn(0.5f, 3.0f)
                atualizarTextoSensibilidade(valor)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Config.sensibilidadeMouse = (barraSensibilidade.progress / 10.0f).coerceIn(0.5f, 3.0f)
            }
        })

        findViewById<Button>(R.id.botaoAplicarConfig).setOnClickListener {
            aplicarConfiguracoes()
        }

        findViewById<Button>(R.id.botaoRestaurarPadroes).setOnClickListener {
            restaurarPadroes()
        }

        findViewById<Button>(R.id.botaoFecharConfig).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.botaoNovoPerfil).setOnClickListener {
            criarNovoPerfil()
        }

        findViewById<Button>(R.id.botaoSalvarPerfil).setOnClickListener {
            Toast.makeText(this, "💾 Perfil salvo!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.botaoCarregarPerfil).setOnClickListener {
            val nomePerfil = seletorPerfil.selectedItem.toString()
            Config.perfilAtual = nomePerfil
            Toast.makeText(this, "📂 Perfil \"$nomePerfil\" carregado!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun aplicarConfiguracoes() {
        val progress = barraSensibilidade.progress
        Config.sensibilidadeMouse = (progress / 10.0f).coerceIn(0.5f, 3.0f)

        val modoSelecionado = findViewById<RadioGroup>(R.id.grupoModoMouse).checkedRadioButtonId
        Config.modoMouse = if (modoSelecionado == R.id.modoCamera) "CÂMERA" else "CURSOR"

        Toast.makeText(this, "✅ Configurações aplicadas!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun restaurarPadroes() {
        AlertDialog.Builder(this)
            .setTitle("Restaurar Padrões")
            .setMessage("Tem certeza? Todas as configurações voltarão ao original.")
            .setPositiveButton("Sim") { _, _ ->
                Config.sensibilidadeMouse = 1.5f
                Config.modoMouse = "CÂMERA"
                carregarValoresSalvos()
                Toast.makeText(this, "↩️ Restaurado!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun criarNovoPerfil() {
        AlertDialog.Builder(this)
            .setTitle("Novo Perfil")
            .setMessage("Digite o nome do jogo/perfil:")
            .setView(android.widget.EditText(this).apply { hint = "Nome do jogo" })
            .setPositiveButton("Criar") { dialog, _ ->
                val entrada = (dialog as AlertDialog).findViewById<EditText>(android.R.id.edit)
                val nome = entrada?.text?.toString()?.trim() ?: return
