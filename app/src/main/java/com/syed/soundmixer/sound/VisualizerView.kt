package com.syed.soundmixer.sound

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
class VisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.parseColor("#70bcf0")
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val amplitudes = mutableListOf<Int>()
    private var lastAmplitude = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWave(canvas)
    }

    private fun drawWave(canvas: Canvas) {
        if (amplitudes.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val midHeight = height / 2

        // Drawing waveform as a line
        val path = android.graphics.Path()
        val sectionWidth = width / (amplitudes.size - 1)

        path.moveTo(0f, midHeight - normalizeAmplitude(amplitudes[0]))

        for (i in 1 until amplitudes.size) {
            val x = i * sectionWidth
            val y = midHeight - normalizeAmplitude(amplitudes[i])
            path.lineTo(x, y)
        }

        // Draw line to the end to ensure the waveform ends correctly
        path.lineTo(width, midHeight)
        canvas.drawPath(path, paint)
    }

    private fun normalizeAmplitude(amplitude: Int): Float {
        val maxAmplitude = 32767 // Max amplitude value for 16-bit PCM audio
        val scaledAmplitude = amplitude.toFloat() / maxAmplitude
        return scaledAmplitude * height / 2
    }

    fun updateAmplitude(amplitude: Int) {
        if (amplitudes.size >= width / 10) {
            amplitudes.removeAt(0)
        }
        amplitudes.add(amplitude)
        lastAmplitude = (lastAmplitude * 0.9 + amplitude * 0.1).toInt()
        amplitudes.add(lastAmplitude)
        invalidate()
    }

    fun reset() {
        amplitudes.clear()
        invalidate()  // Redraw the view to clear it
    }
}

