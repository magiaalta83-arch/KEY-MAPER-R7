package com.seu.meumouse

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast

class OtimizadorSistema(private val contexto: Context) {

    fun verificarOtimizacaoBateria(): Boolean {
        val pm = contexto.getSystemService(Context.POWER_SERVICE) as PowerManager
        val nomePacote = contexto.packageName
        return pm.isIgnoringBatteryOptimizations(nomePacote)
    }

    fun pedirExcecaoBateria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${contexto.packageName}")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            contexto.startActivity(intent)
        }
    }

    fun manterServicoRodando() {
        // Evita que o sistema feche o app em segundo plano
        val pm = contexto.getSystemService(Context.POWER_SERVICE) as PowerManager
        val bloqueio = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TeclaR7:Servico")
        if (!bloqueio.isHeld) bloqueio.acquire(10*60*1000L /*10 min*/)
    }

    fun verificarPermissoesEssenciais(): List<String> {
        val problemas = mutableListOf<String>()
        
        if (!verificarOtimizacaoBateria()) {
            problemas.add("⚠️ Otimização de bateria ativa → app pode fechar sozinho")
        }
        
        if (!verificarPermissaoSobreporTelas()) {
            problemas.add("⚠️ Permissão de desenhar sobre apps necessária")
        }
        
        return problemas
    }

    private fun verificarPermissaoSobreporTelas(): Boolean {
        return Settings.canDrawOverlays(contexto)
    }

    fun abrirTelaPermissoes() {
        if (!verificarPermissaoSobreporTelas()) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${contexto.packageName}")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            contexto.startActivity(intent)
        }
    }
}
