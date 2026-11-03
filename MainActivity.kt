package com.seu.meumouse

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val CODIGO_PERMISSAO_SOBREPOSICAO = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verificarPermissoes()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val comando = when (keyCode) {
            KeyEvent.KEYCODE_W -> "W"
            KeyEvent.KEYCODE_A -> "A"
            KeyEvent.KEYCODE_S -> "S"
            KeyEvent.KEYCODE_D -> "D"
            KeyEvent.KEYCODE_SPACE -> "SPACE"
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> "SHIFT_LEFT"
            KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT -> "CTRL_LEFT"
            KeyEvent.KEYCODE_F1 -> "SALVAR_CONFIG"
            KeyEvent.KEYCODE_F2 -> "CARREGAR_CONFIG"
            KeyEvent.KEYCODE_F3 -> "RESETAR_TUDO"
            else -> null
        }
        comando?.let { enviarComando(it) }
        return super.onKeyDown(keyCode, event)
    }

    private fun enviarComando(comando: String) {
        val intent = Intent("com.seu.meumouse.COMANDO")
        intent.putExtra("comando", comando)
        sendBroadcast(intent)
    }

    private fun verificarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, CODIGO_PERMISSAO_SOBREPOSICAO)
            } else {
                Toast.makeText(this, "✅ Permissões OK! Ative o serviço de Acessibilidade.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
