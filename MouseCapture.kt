package com.seu.meumouse

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.InputDevice
import android.view.MotionEvent
import kotlin.math.abs

class MouseCapture(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("config_mapeador", Context.MODE_PRIVATE)
    }

    var sensibilidade: Float
        get() = prefs.getFloat("sensibilidade_mouse", 1.5f)
        set(value) = prefs.edit().putFloat("sensibilidade_mouse", value.coerceIn(0.5f, 3.0f)).apply()

    var suavizacao: Float
        get() = prefs.getFloat("suavizacao_mouse", 0.8f)
        set(value) = prefs.edit().putFloat("suavizacao_mouse", value.coerceIn(0.3f, 0.95f)).apply()

    var modoMouse: String
        get() = prefs.getString("modo_mouse", "DESLIGADO") ?: "DESLIGADO"
        set(value) = prefs.edit().putString("modo_mouse", value).apply()

    private var ultimoX = 0f
    private var ultimoY = 0f
    private var velocidadeX = 0f
    private var velocidadeY = 0f
    private var primeiroMovimento = true
    private var mouseConectado = false

    private val historicoDeltaX = ArrayDeque<Float>(5)
    private val historicoDeltaY = ArrayDeque<Float>(5)

    fun verificarDispositivo(evento: MotionEvent): Boolean {
        val fonte = evento.source
        mouseConectado = (fonte and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) ||
                         (fonte and InputDevice.SOURCE_MOUSE_RELATIVE == InputDevice.SOURCE_MOUSE_RELATIVE)
        return mouseConectado
    }

    fun processarMovimento(evento: MotionEvent, callback: (Float, Float) -> Unit) {
        if (modoMouse != "CAMERA" || !mouseConectado) return

        val acao = evento.actionMasked
        when (acao) {
            MotionEvent.ACTION_DOWN -> {
                ultimoX = evento.rawX
                ultimoY = evento.rawY
                primeiroMovimento = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!primeiroMovimento) {
                    var deltaX = (evento.rawX - ultimoX) * sensibilidade
                    var deltaY = (evento.rawY - ultimoY) * sensibilidade

                    val dispositivo = evento.device
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && dispositivo != null) {
                        val eixoRelX = evento.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
                        val eixoRelY = evento.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
                        if (abs(eixoRelX) > 0.001f || abs(eixoRelY) > 0.001f) {
                            deltaX = eixoRelX * sensibilidade * 25f
                            deltaY = eixoRelY * sensibilidade * 25f
                        }
                    }

                    historicoDeltaX.add(deltaX)
                    historicoDeltaY.add(deltaY)
                    while (historicoDeltaX.size > 5) historicoDeltaX.removeFirst()
                    while (historicoDeltaY.size > 5) historicoDeltaY.removeFirst()

                    val mediaX = calcularMediaPonderada(historicoDeltaX)
                    val mediaY = calcularMediaPonderada(historicoDeltaY)

                    velocidadeX = (velocidadeX * suavizacao) + (mediaX * (1 - suavizacao))
                    velocidadeY = (velocidadeY * suavizacao) + (mediaY * (1 - suavizacao))

                    if (abs(velocidadeX) > 0.3f || abs(velocidadeY) > 0.3f) {
                        callback(velocidadeX, velocidadeY)
                    }
                } else {
                    primeiroMovimento = false
                }
                ultimoX = evento.rawX
                ultimoY = evento.rawY
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocidadeX = 0f
                velocidadeY = 0f
                historicoDeltaX.clear()
                historicoDeltaY.clear()
                primeiroMovimento = true
            }
        }
    }

    private fun calcularMediaPonderada(lista: ArrayDeque<Float>): Float {
        if (lista.isEmpty()) return 0f
        var soma = 0f
        var pesoTotal = 0f
        lista.forEachIndexed { i, valor ->
            val peso = (i + 1).toFloat()
            soma += valor * peso
            pesoTotal += peso
        }
        return if (pesoTotal > 0) soma / pesoTotal else 0f
    }

    fun resetar() {
        ultimoX = 0f
        ultimoY = 0f
        velocidadeX = 0f
        velocidadeY = 0f
        primeiroMovimento = true
        historicoDeltaX.clear()
        historicoDeltaY.clear()
    }
}
