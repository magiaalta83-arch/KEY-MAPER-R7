package com.seu.meumouse

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AppPrincipal : Application() {
    
    companion object {
        lateinit var instancia: AppPrincipal
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instancia = this
        criarCanalNotificacao()
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "servico_principal",
                "Serviço de Mapeamento",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantém o mapeamento de teclas ativo"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }
}
