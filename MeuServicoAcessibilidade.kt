package com.seu.meumouse

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

data class BotaoMapeado(
    val id: String,
    var x: Float,
    var y: Float,
    val tecla: String,
    val icone: String
)

class MeuServicoAcessibilidade : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private val botoesAtivos = mutableMapOf<String, View>()
    
    private val configuracao = mutableListOf(
        BotaoMapeado("atirar", 500f, 1500f, "SPACE", "🔫"),
        BotaoMapeado("cima", 500f, 800f, "W", "⬆️"),
        BotaoMapeado("baixo", 500f, 1200f, "S", "⬇️"),
        BotaoMapeado("esquerda", 300f, 1000f, "A", "⬅️"),
        BotaoMapeado("direita", 700f, 1000f, "D", "➡️"),
        BotaoMapeado("pular", 500f, 600f, "SHIFT_LEFT", "🦘"),
        BotaoMapeado("agachar", 200f, 1500f, "CTRL_LEFT", "🧎")
    )

    private val receptorComandos = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra("comando")?.let { comando ->
                when (comando) {
                    "SALVAR_CONFIG" -> salvarConfiguracoes()
                    "CARREGAR_CONFIG" -> carregarConfiguracoes()
                    "RESETAR_TUDO" -> resetarTudo()
                    else -> dispararToquePorComando(comando)
                }
            }
        }
    }

    private fun dispararToquePorComando(comando: String) {
        val botao = configuracao.find { it.tecla == comando }
        botao?.let { simularToque(it.x + 24f, it.y + 24f) }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this, "🎮 Tecla do Mouse R7 — Pronto!", Toast.LENGTH_SHORT).show()
        registerReceiver(receptorComandos, IntentFilter("com.seu.meumouse.COMANDO"))
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        criarTodosBotoes()
    }

    private fun criarTodosBotoes() {
        configuracao.forEach { dadosBotao ->
            criarBotaoFlutuante(dadosBotao)
        }
    }

    private fun criarBotaoFlutuante(dados: BotaoMapeado) {
        val layoutTipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val botaoView = LayoutInflater.from(this).inflate(R.layout.botao_flutuante, null)
        val icone = botaoView.findViewById<Button>(R.id.botaoIcone)
        val labelTecla = botaoView.findViewById<TextView>(R.id.teclaAtribuida)
        
        icone.text = dados.icone
        labelTecla.text = when(dados.tecla) {
            "SPACE" -> "ESP"
            "SHIFT_LEFT" -> "SHFT"
            "CTRL_LEFT" -> "CTRL"
            else -> dados.tecla
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutTipo,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = dados.x.toInt()
        params.y = dados.y.toInt()

        windowManager.addView(botaoView, params)
        botoesAtivos[dados.id] = botaoView

        var inicialX = 0
        var inicialY = 0
        botaoView.setOnTouchListener { _, evento ->
            when (evento.action) {
                MotionEvent.ACTION_DOWN -> {
                    inicialX = evento.rawX.toInt() - params.x
                    inicialY = evento.rawY.toInt() - params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = evento.rawX.toInt() - inicialX
                    params.y = evento.rawY.toInt() - inicialY
                    dados.x = params.x.toFloat()
                    dados.y = params.y.toFloat()
                    windowManager.updateViewLayout(botaoView, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun simularToque(x: Float, y: Float, duracao: Long = 80) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val caminho = Path()
            caminho.moveTo(x, y)
            val gesto = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(caminho, 0, duracao))
                .build()
            dispatchGesture(gesto, null, null)
        }
    }

    private fun salvarConfiguracoes() {
        val prefs = getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        configuracao.forEach {
            editor.putFloat("${it.id}_x", it.x)
            editor.putFloat("${it.id}_y", it.y)
        }
        editor.apply()
        Toast.makeText(this, "✅ Configurações salvas!", Toast.LENGTH_SHORT).show()
    }

    private fun carregarConfiguracoes() {
        val prefs = getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
        configuracao.forEach {
            it.x = prefs.getFloat("${it.id}_x", it.x)
            it.y = prefs.getFloat("${it.id}_y", it.y)
        }
        atualizarPosicoesNaTela()
        Toast.makeText(this, "✅ Configurações carregadas!", Toast.LENGTH_SHORT).show()
    }

    private fun atualizarPosicoesNaTela() {
        configuracao.forEach { dados ->
            botoesAtivos[dados.id]?.let { view ->
                val params = view.layoutParams as WindowManager.LayoutParams
                params.x = dados.x.toInt()
                params.y = dados.y.toInt()
                windowManager.updateViewLayout(view, params)
            }
        }
    }

    private fun resetarTudo() {
        botoesAtivos.values.forEach { windowManager.removeView(it) }
        botoesAtivos.clear()
        configuracao.forEach {
            it.x = 500f
            it.y = 1000f
        }
        criarTodosBotoes()
        Toast.makeText(this, "↩️ Posições resetadas!", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    
    override fun onInterrupt() {
        unregisterReceiver(receptorComandos)
        botoesAtivos.values.forEach { windowManager.removeView(it) }
        botoesAtivos.clear()
    }
}
