package com.seu.meumouse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class TelaLoginActivity : AppCompatActivity() {

    private val RC_GOOGLE = 1001
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        // Configurar Login com Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.botaoLoginGoogle).setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_GOOGLE)
        }

        findViewById<Button>(R.id.botaoLoginGithub).setOnClickListener {
            Toast.makeText(this, "🔄 Abrindo GitHub...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TelaPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE) {
            val tarefa = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val conta = tarefa.getResult(ApiException::class.java)
                Toast.makeText(this, "✅ Bem-vindo, ${conta.email}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: ApiException) {
                Toast.makeText(this, "❌ Falha no login", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
