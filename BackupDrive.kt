package com.seu.meumouse

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject

class BackupDrive(private val contexto: Context) {

    private val prefs: SharedPreferences by lazy {
        contexto.getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
    }

    fun gerarJsonConfiguracoes(): String {
        val json = JSONObject()
        json.put("sensibilidade", prefs.getFloat("sensibilidade_mouse", 1.5f))
        json.put("suavizacao", prefs.getFloat("suavizacao_mouse", 0.8f))
        json.put("modo_mouse", prefs.getString("modo_mouse", "CAMERA"))
        
        val botoes = JSONObject()
        val nomes = listOf("atirar", "cima", "baixo", "esquerda", "direita", "pular", "agachar")
        nomes.forEach { id ->
            val botao = JSONObject()
            botao.put("x", prefs.getFloat("${id}_x", 500f))
            botao.put("y", prefs.getFloat("${id}_y", 800f))
            botoes.put(id, botao)
        }
        json.put("botoes", botoes)
        json.put("versao_app", "1.0")
        return json.toString(2)
    }

    fun salvarArquivo(activity: Activity, nomeArquivo: String = "config_teclado_r7.json") {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, nomeArquivo)
        }
        activity.startIntentSenderForResult(
            intent.intentSender,
            CODIGO_SALVAR,
            null, 0, 0, 0, null
        )
    }

    fun escreverNoUri(uri: Uri, conteudo: String): Boolean {
        return try {
            contexto.contentResolver.openOutputStream(uri)?.use { saida ->
                saida.write(conteudo.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restaurarDeUri(uri: Uri): Boolean {
        return try {
            val json = contexto.contentResolver.openInputStream(uri)?.bufferedReader().use {
                it?.readText()
            } ?: return false
            
            val obj = JSONObject(json)
            val ed = prefs.edit()
            
            ed.putFloat("sensibilidade_mouse", obj.optDouble("sensibilidade", 1.5).toFloat())
            ed.putFloat("suavizacao_mouse", obj.optDouble("suavizacao", 0.8).toFloat())
            ed.putString("modo_mouse", obj.optString("modo_mouse", "CAMERA"))
            
            val botoes = obj.optJSONObject("botoes")
            if (botoes != null) {
                val nomes = listOf("atirar", "cima", "baixo", "esquerda", "direita", "pular", "agachar")
                nomes.forEach { id ->
                    val botao = botoes.optJSONObject(id)
                    if (botao != null) {
                        ed.putFloat("${id}_x", botao.optDouble("x", 500.0).toFloat())
                        ed.putFloat("${id}_y", botao.optDouble("y", 800.0).toFloat())
                    }
                }
            }
            ed.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val CODIGO_SALVAR = 2001
        const val CODIGO_ABRIR = 2002
    }
}
