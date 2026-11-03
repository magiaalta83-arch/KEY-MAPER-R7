package com.seu.meumouse

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditorTeclasActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOME_BOTAO = "nome_botao"
        const val EXTRA_TECLA_ATUAL = "tecla_atual"
        const val EXTRA_ID_BOTAO = "id_botao"
    }

    private lateinit var textoNomeBotao: TextView
    private lateinit var textoTeclaAtual: TextView
    private var teclaSelecionada: String? = null
    private var idBotao: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_editar_tecla)

        textoNomeBotao = findViewById(R.id.nomeBotaoEdicao)
        textoTeclaAtual = findViewById(R.id.teclaAtualTexto)

        val nomeBotao = intent.getStringExtra(EXTRA_NOME_BOTAO) ?: "Botão"
        val teclaAtual = intent.getStringExtra(EXTRA_TECLA_ATUAL) ?: "?"
        idBotao = intent.getStringExtra(EXTRA_ID_BOTAO) ?: ""

        textoNomeBotao.text = nomeBotao
        textoTeclaAtual.text = teclaAtual
        teclaSelecionada = teclaAtual

        findViewById<Button>(R.id.botaoConfirmarTecla).setOnClickListener { confirmar() }
        findViewById<Button>(R.id.botaoCancelarTecla).setOnClickListener { finish() }
    }

    override fun onKeyDown(codigoTecla: Int, evento: KeyEvent): Boolean {
        val nomeTecla = quandoPressionar(codigoTecla)
        if (nomeTecla != null) {
            teclaSelecionada = nomeTecla
            textoTeclaAtual.text = nomeTecla
        }
        return super.onKeyDown(codigoTecla, evento)
    }

    private fun quandoPressionar(codigo: Int): String? {
        return when (codigo) {
            KeyEvent.KEYCODE_SPACE -> "SPACE"
            KeyEvent.KEYCODE_SHIFT_LEFT -> "SHIFT_LEFT"
            KeyEvent.KEYCODE_SHIFT_RIGHT -> "SHIFT_LEFT"
            KeyEvent.KEYCODE_CTRL_LEFT -> "CTRL_LEFT"
            KeyEvent.KEYCODE_CTRL_RIGHT -> "CTRL_LEFT"
            KeyEvent.KEYCODE_ALT_LEFT -> "ALT"
            KeyEvent.KEYCODE_W -> "W"
            KeyEvent.KEYCODE_A -> "A"
            KeyEvent.KEYCODE_S -> "S"
            KeyEvent.KEYCODE_D -> "D"
            KeyEvent.KEYCODE_E -> "E"
            KeyEvent.KEYCODE_Q -> "Q"
            KeyEvent.KEYCODE_F1 -> "F1"
            KeyEvent.KEYCODE_F2 -> "F2"
            KeyEvent.KEYCODE_F3 -> "F3"
            KeyEvent.KEYCODE_F4 -> "F4"
            KeyEvent.KEYCODE_F5 -> "F5"
            KeyEvent.KEYCODE_ESCAPE -> "ESC"
            KeyEvent.KEYCODE_BACK -> "VOLTAR"
            else -> null
        }
    }

    private fun confirmar() {
        if (teclaSelecionada == null) {
            Toast.makeText(this, "Pressione uma tecla primeiro!", Toast.LENGTH_SHORT).show()
            return
        }
        val resultado = Intent()
        resultado.putExtra("nova_tecla", teclaSelecionada)
        resultado.putExtra("id_botao", idBotao)
        setResult(RESULT_OK, resultado)
        finish()
    }
}
