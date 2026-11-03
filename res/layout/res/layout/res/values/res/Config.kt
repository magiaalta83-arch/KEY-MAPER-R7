package com.seu.meumouse

import android.content.Context
import android.content.SharedPreferences

object Config {
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
    }

    var sensibilidadeMouse: Float
        get() = prefs.getFloat("sensibilidade_mouse", 1.5f)
        set(value) = prefs.edit().putFloat("sensibilidade_mouse", value.coerceIn(0.5f, 3.0f)).apply()

    var sensibilidadeToque: Float
        get() = prefs.getFloat("sensibilidade_toque", 1.0f)
        set(value) = prefs.edit().putFloat("sensibilidade_toque", value.coerceIn(0.5f, 2.5f)).apply()

    var modoMouse: String
        get() = prefs.getString("modo_mouse", "CÂMERA") ?: "CÂMERA"
        set(value) = prefs.edit().putString("modo_mouse", value).apply()

    var botaoDireitoPressiona: Boolean
        get() = prefs.getBoolean("botao_direito_pressiona", true)
        set(value) = prefs.edit().putBoolean("botao_direito_pressiona", value).apply()

    var perfilAtual: String
        get() = prefs.getString("perfil_atual", "PADRÃO") ?: "PADRÃO"
        set(value) = prefs.edit().putString("perfil_atual", value).apply()

    fun salvarPerfil(nome: String, botoes: List<BotaoMapeado>) {
        val editor = prefs.edit()
        botoes.forEach {
            editor.putFloat("perfil_${nome}_${it.id}_x", it.x)
            editor.putFloat("perfil_${nome}_${it.id}_y", it.y)
        }
        editor.putString("perfil_atual", nome)
        editor.apply()
    }

    fun carregarPerfil(nome: String, botoes: MutableList<BotaoMapeado>) {
        botoes.forEach {
            it.x = prefs.getFloat("perfil_${nome}_${it.id}_x", it.x)
            it.y = prefs.getFloat("perfil_${nome}_${it.id}_y", it.y)
        }
        perfilAtual = nome
    }

    fun listarPerfis(): Set<String> {
        val todos = prefs.all.keys
        return todos.mapNotNull {
            if (it.startsWith("perfil_") && !it.startsWith("perfil_atual")) {
                it.split("_")[1]
            } else null
        }.toSet()
    }
}
