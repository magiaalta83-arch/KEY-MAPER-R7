package com.seu.meumouse

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TelaConfiguracoesActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var barraSensibilidade: SeekBar
    private lateinit var valorSensibilidade: TextView
    private lateinit var barraSuavizacao: SeekBar
    private lateinit var valorSuavizacao: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_configuracoes)

        prefs = getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
        inicializar()
        carregarValores()
        configurarBotoes()
    }

    private fun inicializar() {
        barraSensibilidade = findViewById(R.id.barraSensibilidade)
        valorSensibilidade = findViewById(R.id.valorSensibilidade)
        barraSuavizacao = findViewById(R.id.barraSuavizacao)
        valorSuavizacao = findViewById(R.id.valorSuavizacao)
    }

    private fun carregarValores() {
        val sens = prefs.getFloat("sensibilidade_mouse", 1.5f)
        barraSensibilidade.progress = (sens * 10).toInt()
        atualizarTextoSensibilidade(sens)

        val suav = prefs.getFloat("suavizacao_mouse", 0.8f)
        barraSuavizacao.progress = (suav * 100).toInt()
        atualizarTextoSuavizacao(suav)

        val modo = prefs.getString("modo_mouse", "CAMERA")
        findViewById<RadioButton>(
            if (modo == "CAMERA") R.id.modoCamera else R.id.modoCursor
        ).isChecked = true
    }

    private fun atualizarTextoSensibilidade(valor: Float) {
        valorSensibilidade.text = String.format("%.1fx", valor)
    }

    private fun atualizarTextoSuavizacao(valor: Float) {
        valorSuavizacao.text = String.format("%.0f%%", valor * 100)
    }

    private fun configurarBotoes() {
        barraSensibilidade.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val valor = (p / 10.0f).coerceIn(0.5f, 3.0f)
                atualizarTextoSensibilidade(valor)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                val valor = (s?.progress?.div(10.0f) ?: 1.5f).coerceIn(0.5f, 3.0f)
                prefs.edit().putFloat("sensibilidade_mouse", valor).apply()
            }
        })

        barraSuavizacao.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val valor = (p / 100.0f).coerceIn(0.3f, 0.95f)
                atualizarTextoSuavizacao(valor)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                val valor = (s?.progress?.div(100.0f) ?: 0.8f).coerceIn(0.3f, 0.95f)
                prefs.edit().putFloat("suavizacao_mouse", valor).apply()
            }
        })

        findViewById<Button>(R.id.botaoAplicarConfig).setOnClickListener {
            val modo = findViewById<RadioGroup>(R.id.grupoModoMouse).checkedRadioButtonId
            prefs.edit()
                .putString("modo_mouse", if (modo == R.id.modoCamera) "CAMERA" else "CURSOR")
                .apply()
            Toast.makeText(this, "✅ Configurações salvas!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.botaoRestaurarPadroes).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restaurar Padrões")
                .setMessage("Voltar tudo ao original?")
                .setPositiveButton("Sim") { _, _ ->
                    prefs.edit().clear().apply()
                    carregarValores()
                    Toast.makeText(this, "↩️ Restaurado!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Não", null)
                .show()
        }

        findViewById<Button>(R.id.botaoFecharConfig).setOnClickListener { finish() }
    }
}
