package dev.starstream.prototype

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import android.widget.FrameLayout
import kotlin.math.sin

/** Visual prototype: rounded holographic edges, dark drop shadow, scanlines and brief glitch pulses. */
class HologramPanel(context: Context) : FrameLayout(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private val inset = 17f * density
    private val corner = 19f * density
    private val started = SystemClock.uptimeMillis()
    private var framesRemaining = 13

    init {
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        post { glitch() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x1 = inset
        val y1 = inset
        val x2 = width - inset
        val y2 = height - inset
        val rect = RectF(x1, y1, x2, y2)
        if (x2 <= x1 || y2 <= y1) return

        p.style = Paint.Style.FILL
        p.color = Color.argb(236, 8, 37, 81)
        p.setShadowLayer(16f * density, 0f, 9f * density, Color.argb(225, 0, 0, 0))
        canvas.drawRoundRect(rect, corner, corner, p)
        p.clearShadowLayer()

        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f * density
        p.color = Color.rgb(73, 213, 255)
        p.setShadowLayer(12f * density, 0f, 0f, Color.rgb(33, 176, 255))
        canvas.drawRoundRect(rect, corner, corner, p)
        p.clearShadowLayer()

        p.strokeWidth = 1f * density
        p.color = Color.argb(185, 213, 247, 255)
        canvas.drawRoundRect(RectF(x1 + 4f * density, y1 + 4f * density, x2 - 4f * density, y2 - 4f * density), corner - 4f * density, corner - 4f * density, p)

        // Fine horizontal phosphor lines across the panel.
        p.style = Paint.Style.STROKE
        p.strokeWidth = density.coerceAtLeast(1f) * .42f
        p.color = Color.argb(26, 146, 232, 255)
        var y = y1 + 8f * density
        while (y < y2 - 8f * density) {
            canvas.drawLine(x1 + 8f * density, y, x2 - 8f * density, y, p)
            y += 6f * density
        }

        // Discrete horizontal digital interference, stronger during entry.
        val first = (SystemClock.uptimeMillis() - started) < 550L
        p.style = Paint.Style.FILL
        for (i in 0 until if (first) 8 else 3) {
            val fy = y1 + 20f * density + ((i * 47) % ((y2 - y1 - 40f * density).toInt().coerceAtLeast(1)))
            val fx = if (i % 2 == 0) x1 - 3f * density else x2 - 46f * density
            p.color = if (i % 3 == 0) Color.argb(130, 167, 235, 255) else Color.argb(72, 37, 218, 255)
            canvas.drawRect(fx, fy, fx + (14 + i * 4) * density, fy + (1 + (i % 2)) * density, p)
        }
    }

    private fun glitch() {
        if (framesRemaining-- <= 0) return
        invalidate()
        postDelayed({ glitch() }, 48L)
    }
}
