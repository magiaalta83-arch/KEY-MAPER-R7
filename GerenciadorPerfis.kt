package com.seu.meumouse

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences

data class PerfilJogo(
    val id: String,
    val nome: String,
    val pacote: String,
    val sensibilidade: Float,
    val botoes: MutableList<BotaoMapeado>,
    val icone: String = "🎮"
)

class GerenciadorPerfis(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("perfis_jogo", Context.MODE_PRIVATE)
    }
    private val prefsConfig: SharedPreferences by lazy {
        context.getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
    }

    private var ultimoPacote: String = ""
    
    var deteccaoAutomaticaAtiva: Boolean
        get() = prefs.getBoolean("detectar_jogo_auto", true)
        set(value) = prefs.edit().putBoolean("detectar_jogo_auto", value).apply()

    fun detectarJogoAtual(): String? {
        if (!deteccaoAutomaticaAtiva) return null
        
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tarefas = am.getRunningTasks(1)
        if (tarefas.isNotEmpty()) {
            val pacote = tarefas[0].topActivity?.packageName ?: return null
            if (pacote != ultimoPacote && pacote != context.packageName) {
                ultimoPacote = pacote
                return pacote
            }
        }
        return null
    }

    fun verificarETrocarPerfil(): PerfilJogo? {
        val pacote = detectarJogoAtual() ?: return null
        return carregarPerfilPorPacote(pacote)?.also { perfil ->
            aplicarPerfil(perfil)
        }
    }

    fun criarPerfil(nome: String, pacote: String, botoes: List<BotaoMapeado>): PerfilJogo {
        val id = "perfil_${System.currentTimeMillis()}"
        val perfil = PerfilJogo(
            id = id,
            nome = nome,
            pacote = pacote,
            sensibilidade = prefsConfig.getFloat("sensibilidade_mouse", 1.5f),
            botoes = botoes.map { it.copy() }.toMutableList()
        )
        salvarPerfil(perfil)
        return perfil
    }

    fun salvarPerfil(perfil: PerfilJogo) {
        val ed = prefs.edit()
        ed.putString("nome_${perfil.id}", perfil.nome)
        ed.putString("pacote_${perfil.id}", perfil.pacote)
        ed.putFloat("sens_${perfil.id}", perfil.sensibilidade)
        ed.putString("icone_${perfil.id}", perfil.icone)
        ed.putInt("qtd_${perfil.id}", perfil.botoes.size)
        perfil.botoes.forEachIndexed { i, btn ->
            ed.putString("btn_${perfil.id}_$i", "${btn.id}|${btn.x}|${btn.y}|${btn.tecla}|${btn.icone}")
        }
        ed.apply()
    }

    fun carregarPerfil(id: String): PerfilJogo? {
        val nome = prefs.getString("nome_$id", null) ?: return null
        val pacote = prefs.getString("pacote_$id", "")
        val sens = prefs.getFloat("sens_$id", 1.5f)
        val icone = prefs.getString("icone_$id", "🎮")
        val qtd = prefs.getInt("qtd_$id", 0)
        
        val botoes = mutableListOf<BotaoMapeado>()
        for (i in 0 until qtd) {
            val dados = prefs.getString("btn_${id}_$i", null) ?: continue
            val p = dados.split("|")
            if (p.size >= 5) {
                botoes.add(BotaoMapeado(p[0], p[1].toFloat(), p[2].toFloat(), p[3], p[4]))
            }
        }
        return PerfilJogo(id, nome, pacote, sens, botoes, icone)
    }

    fun carregarPerfilPorPacote(pacote: String): PerfilJogo? {
        return listarTodos().firstOrNull { it.pacote == pacote }
    }

    fun listarTodos(): List<PerfilJogo> {
        return prefs.all.keys
            .filter { it.startsWith("nome_") }
            .mapNotNull { carregarPerfil(it.removePrefix("nome_")) }
    }

    fun aplicarPerfil(perfil: PerfilJogo) {
        prefsConfig.edit().putFloat("sensibilidade_mouse", perfil.sensibilidade).apply()
        val ed = prefsConfig.edit()
        perfil.botoes.forEach { btn ->
            ed.putFloat("${btn.id}_x", btn.x)
            ed.putFloat("${btn.id}_y", btn.y)
        }
        ed.apply()
    }

    fun excluirPerfil(id: String): Boolean {
        val ed = prefs.edit()
        prefs.all.keys.filter { it.endsWith("_$id") || it.contains("${id}_") }.forEach { ed.remove(it) }
        return ed.commit()
    }

    fun perfilPadrao(): PerfilJogo {
        return PerfilJogo(
            id = "padrao",
            nome = "PADRÃO",
            pacote = "",
            sensibilidade = 1.5f,
            botoes = mutableListOf(
                BotaoMapeado("atirar", 500f, 1500f, "SPACE", "🔫"),
                BotaoMapeado("cima", 500f, 800f, "W", "⬆️"),
                BotaoMapeado("baixo", 500f, 1200f, "S", "⬇️"),
                BotaoMapeado("esquerda", 300f, 1000f, "A", "⬅️"),
                BotaoMapeado("direita", 700f, 1000f, "D", "➡️"),
                BotaoMapeado("pular", 500f, 600f, "SHIFT_LEFT", "🦘"),
                BotaoMapeado("agachar", 200f, 1500f, "CTRL_LEFT", "🧎")
            )
        )
    }
}
