package com.seu.meumouse

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

data class CalibragemJogo(
    val pacote: String,
    val nome: String,
    val sensibilidadeRecomendada: Float,
    val botoes: Map<String, Pair<Float, Float>>,
    val dpiRecomendado: Int
)

class DetectorJogo(private val contexto: Context) {

    private val prefs: SharedPreferences by lazy {
        contexto.getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
    }

    // 🎯 BASE COMPLETA DE JOGOS — adicione mais aqui quando precisar
    private val baseCalibragem = listOf(

        // 🔥 Jogos de Tiro / Battle Royale
        CalibragemJogo(
            pacote = "com.dts.freefireth",
            nome = "Free Fire",
            sensibilidadeRecomendada = 1.4f,
            botoes = mapOf(
                "atirar" to Pair(540f, 1600f),
                "pular" to Pair(950f, 1400f),
                "agachar" to Pair(80f, 1400f),
                "mirar" to Pair(850f, 1600f)
            ),
            dpiRecomendado = 480
        ),
        CalibragemJogo(
            pacote = "com.tencent.ig",
            nome = "PUBG Mobile",
            sensibilidadeRecomendada = 1.3f,
            botoes = mapOf(
                "atirar" to Pair(550f, 1580f),
                "pular" to Pair(920f, 1380f),
                "agachar" to Pair(70f, 1380f),
                "mirar" to Pair(860f, 1580f)
            ),
            dpiRecomendado = 450
        ),
        CalibragemJogo(
            pacote = "com.activision.callofduty.shooter",
            nome = "Call of Duty Mobile",
            sensibilidadeRecomendada = 1.5f,
            botoes = mapOf(
                "atirar" to Pair(530f, 1590f),
                "pular" to Pair(930f, 1410f),
                "agachar" to Pair(60f, 1410f),
                "mirar" to Pair(870f, 1590f),
                "arma" to Pair(450f, 1590f)
            ),
            dpiRecomendado = 460
        ),

        // ⚽ Jogos de Esporte
        CalibragemJogo(
            pacote = "com.miniclip.dreamleaguesoccer",
            nome = "Dream League Soccer",
            sensibilidadeRecomendada = 1.8f,
            botoes = mapOf(
                "chutar" to Pair(540f, 1600f),
                "passar" to Pair(300f, 1600f),
                "correr" to Pair(780f, 1600f)
            ),
            dpiRecomendado = 420
        ),
        CalibragemJogo(
            pacote = "com.ea.gp.fifamobile",
            nome = "EA FC Mobile",
            sensibilidadeRecomendada = 1.7f,
            botoes = mapOf(
                "chutar" to Pair(550f, 1580f),
                "passar" to Pair(320f, 1580f),
                "correr" to Pair(790f, 1580f)
            ),
            dpiRecomendado = 420
        ),

        // 🏃 Corredor Infinito
        CalibragemJogo(
            pacote = "com.kiloo.subwaysurf",
            nome = "Subway Surfers",
            sensibilidadeRecomendada = 2.0f,
            botoes = mapOf(
                "pular" to Pair(540f, 1200f),
                "deslizar" to Pair(540f, 1600f),
                "esquerda" to Pair(300f, 1400f),
                "direita" to Pair(780f, 1400f)
            ),
            dpiRecomendado = 400
        ),

        // 🧱 Outros
        CalibragemJogo(
            pacote = "com.mojang.minecraftpe",
            nome = "Minecraft",
            sensibilidadeRecomendada = 1.1f,
            botoes = mapOf(
                "destruir" to Pair(540f, 1600f),
                "colocar" to Pair(800f, 1600f),
                "pular" to Pair(540f, 1200f)
            ),
            dpiRecomendado = 440
        )
    )

    var ultimoPacoteDetectado: String = ""
        private set

    fun detectarJogoEmExecucao(): String? {
        val am = contexto.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tarefas = am.getRunningTasks(1)
        if (tarefas.isNotEmpty()) {
            val pacoteAtual = tarefas[0].topActivity?.packageName ?: return null
            if (pacoteAtual != ultimoPacoteDetectado && pacoteAtual != contexto.packageName) {
                ultimoPacoteDetectado = pacoteAtual
                return pacoteAtual
            }
        }
        return null
    }

    fun obterCalibragem(pacote: String): CalibragemJogo? {
        return baseCalibragem.firstOrNull { it.pacote == pacote }
    }

    fun aplicarCalibragem(calib: CalibragemJogo): Boolean {
        return try {
            val ed = prefs.edit()
            ed.putFloat("sensibilidade_mouse", calib.sensibilidadeRecomendada)
            
            calib.botoes.forEach { (id, pos) ->
                ed.putFloat("${id}_x", pos.first)
                ed.putFloat("${id}_y", pos.second)
            }
            ed.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun verificarEAplicar(): CalibragemJogo? {
        val pacote = detectarJogoEmExecucao() ?: return null
        val calib = obterCalibragem(pacote) ?: return null
        val aplicado = aplicarCalibragem(calib)
        if (aplicado) {
            Toast.makeText(contexto, "✅ ${calib.nome} detectado — calibragem aplicada!", Toast.LENGTH_SHORT).show()
        }
        return calib
    }

    fun todosJogosSuportados(): List<CalibragemJogo> = baseCalibragem
}
