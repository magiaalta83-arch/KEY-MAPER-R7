package com.seu.meumouse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ReinicioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val iniciarServico = Intent(context, MeuServicoAcessibilidade::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(iniciarServico)
            } else {
                context.startService(iniciarServico)
            }
        }
    }
}
