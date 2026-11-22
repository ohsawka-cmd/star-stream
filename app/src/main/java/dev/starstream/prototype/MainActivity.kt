package dev.starstream.prototype

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 15, 28)
        window.navigationBarColor = Color.rgb(8, 15, 28)

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(44), dp(24), dp(24))
            setBackgroundColor(Color.rgb(8, 15, 28))
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }
        fun caption(text: String, size: Float, color: Int = Color.WHITE, marginTop: Int = 0) {
            val view = TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                gravity = Gravity.CENTER
                typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
            }
            screen.addView(view, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(marginTop) })
        }
        caption("[ STAR STREAM // PROTOTYPE 0.1 ]", 15f, Color.rgb(109, 219, 255))
        caption("THE SYSTEM AWAKENS", 31f, Color.WHITE, 25)
        caption("This app tests a floating scenario window over other Android applications.", 17f, Color.rgb(175, 195, 216), 14)

        status = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(161, 220, 245))
            gravity = Gravity.CENTER
        }
        screen.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(30) })

        fun addButton(label: String, onClick: () -> Unit) {
            val button = Button(this).apply {
                text = label
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    setColor(Color.rgb(18, 66, 110))
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(1), Color.rgb(88, 215, 255))
                }
                setOnClickListener { onClick() }
            }
            screen.addView(button, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(14) })
        }
        addButton("1. Allow floating windows") { requestOverlay() }
        addButton("2. Show system scenario") { startScenario() }
        addButton("3. Close system window") {
            startService(Intent(this, SystemOverlayService::class.java).setAction(SystemOverlayService.ACTION_HIDE))
        }
        caption("No AI or network access. This is the first visual-and-permission test, not the complete game.", 14f, Color.rgb(135, 156, 175), 35)
        setContentView(screen)
        maybeAskNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) {
            status.text = if (Settings.canDrawOverlays(this)) "Overlay access: GRANTED" else "Overlay access: NOT YET GRANTED"
        }
    }

    private fun requestOverlay() {
        if (Settings.canDrawOverlays(this)) return
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    private fun startScenario() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlay()
            return
        }
        val intent = Intent(this, SystemOverlayService::class.java).setAction(SystemOverlayService.ACTION_SHOW)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    private fun maybeAskNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 44)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
