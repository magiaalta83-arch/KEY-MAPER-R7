package com.seu.meumouse

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat

data class BotaoMapeado(
    val id: String,
    var x: Float,
    var y: Float,
    val tecla: String,
    val icone: String
)

class MeuServicoAcessibilidade : AccessibilityService() {

    companion object {
        const val ID_NOTIFICACAO = 1001
        var instancia: MeuServicoAcessibilidade? = null
            private set
    }

    private lateinit var windowManager: WindowManager
    private lateinit var prefs: SharedPreferences
    private lateinit var mouseCapture: MouseCapture
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
        instancia = this
        prefs = getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
        mouseCapture = MouseCapture(this)
        
        startForeground(ID_NOTIFICACAO, criarNotificacaoServico())
        
        Toast.makeText(this, "🎮 Tecla do Mouse R7 — Ativo!", Toast.LENGTH_SHORT).show()
        registerReceiver(receptorComandos, IntentFilter("com.seu.meumouse.COMANDO"))
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        carregarConfiguracoes()
        criarTodosBotoes()
    }

    private fun criarNotificacaoServico(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendente = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "servico_principal")
            .setContentTitle("🎮 Tecla do Mouse R7")
            .setContentText("Serviço ativo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendente)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
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
        val icone = botaoView.findViewById<android.widget.Button>(R.id.botaoIcone)
        val labelTecla = botaoView.findViewById<android.widget.TextView>(R.id.teclaAtribuida)
        
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
            mouseCapture.processarMovimento(evento) { _, _ -> }
            
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
        val editor = prefs.edit()
        configuracao.forEach {
            editor.putFloat("${it.id}_x", it.x)
            editor.putFloat("${it.id}_y", it.y)
        }
        editor.apply()
        Toast.makeText(this, "✅ Posições salvas!", Toast.LENGTH_SHORT).show()
    }

    private fun carregarConfiguracoes() {
        configuracao.forEach {
            it.x = prefs.getFloat("${it.id}_x", it.x)
            it.y = prefs.getFloat("${it.id}_y", it.y)
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
