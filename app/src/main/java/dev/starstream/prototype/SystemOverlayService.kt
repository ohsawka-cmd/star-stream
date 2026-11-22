package dev.starstream.prototype

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView

class SystemOverlayService : Service() {
    companion object {
        const val ACTION_SHOW = "dev.starstream.prototype.SHOW"
        const val ACTION_HIDE = "dev.starstream.prototype.HIDE"
        private const val CHANNEL_ID = "star_stream_overlay"
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var windows: WindowManager
    private var panel: HologramPanel? = null
    private var endTime: Long = 0L
    private var countdownText: TextView? = null
    private val tick = object : Runnable {
        override fun run() {
            if (endTime == 0L) return
            val left = (endTime - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            val secs = left / 1000L
            countdownText?.text = "%02d:%02d".format(secs / 60L, secs % 60L)
            if (left == 0L) {
                endTime = 0L
                showResult(false)
            } else handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windows = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HIDE) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Active Star Stream Overlay", NotificationManager.IMPORTANCE_LOW))
        }
        val launch = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)
        val notice = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Star Stream system active")
            .setContentText("Tap to open the app and dismiss the overlay")
            .setContentIntent(launch)
            .setOngoing(true)
            .build()
        startForeground(1, notice)
        showOffer()
        return START_NOT_STICKY
    }

    private fun clearWindow() {
        handler.removeCallbacks(tick)
        endTime = 0L
        countdownText = null
        panel?.let { view ->
            try { windows.removeView(view) } catch (_: IllegalArgumentException) { }
        }
        panel = null
    }

    private fun createPanel(): LinearLayout {
        clearWindow()
        val frame = HologramPanel(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(37), dp(43), dp(37), dp(42))
        }
        frame.addView(content)
        val screenWidth = resources.displayMetrics.widthPixels
        val params = WindowManager.LayoutParams(
            (screenWidth - dp(12)).coerceAtLeast(dp(275)),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }
        windows.addView(frame, params)
        frame.alpha = 0f
        frame.scaleX = .94f
        frame.scaleY = .94f
        frame.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(360L).setInterpolator(DecelerateInterpolator()).start()
        panel = frame
        return content
    }

    private fun text(parent: LinearLayout, message: String, size: Float, color: Int = Color.WHITE,
                     top: Int = 0, bold: Boolean = false): TextView {
        val v = TextView(this).apply {
            this.text = message
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-condensed", if (bold) Typeface.BOLD else Typeface.NORMAL)
            letterSpacing = .015f
        }
        parent.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) })
        return v
    }

    private fun buttons(parent: LinearLayout, primary: String, primaryClick: () -> Unit,
                        secondary: String, secondaryClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun make(label: String, action: () -> Unit) {
            val v = TextView(this).apply {
                text = label
                textSize = 16f
                typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.argb(110, 28, 122, 183))
                setOnClickListener { action() }
                isClickable = true
                contentDescription = label
            }
            row.addView(v, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                setMargins(dp(4), 0, dp(4), 0)
            })
        }
        make(primary, primaryClick)
        make(secondary, secondaryClick)
        parent.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(23) })
    }

    private fun showOffer() {
        val c = createPanel()
        text(c, "[ A NEW SCENARIO HAS ARRIVED. ]", 16f, Color.rgb(214, 248, 255), bold = true)
        text(c, "THE SCHOLAR'S PROOF", 30f, top = 22, bold = true)
        text(c, "Complete 25 minutes of Japanese study.", 19f, top = 18)
        text(c, "Time Limit: 35 minutes after acceptance", 16f, top = 20)
        text(c, "Reward: 35 Coins  ·  50 XP", 17f, Color.rgb(168, 229, 255), top = 9)
        text(c, "[ An unknown constellation is observing you. ]", 15f, Color.rgb(215, 245, 255), top = 25)
        buttons(c, "ACCEPT", { showCountdown() }, "DECLINE", { stopSelf() })
    }

    private fun showCountdown() {
        val c = createPanel()
        text(c, "[ SCENARIO IN PROGRESS ]", 16f, Color.rgb(214, 248, 255), bold = true)
        text(c, "THE SCHOLAR'S PROOF", 28f, top = 21, bold = true)
        text(c, "TIME REMAINING", 17f, top = 18)
        countdownText = text(c, "35:00", 43f, Color.rgb(184, 238, 255), top = 8, bold = true)
        text(c, "Demo: tap COMPLETE after doing the task.", 14f, Color.rgb(193, 208, 222), top = 14)
        buttons(c, "COMPLETE", { showResult(true) }, "CLOSE", { stopSelf() })
        endTime = SystemClock.elapsedRealtime() + 35L * 60L * 1000L
        handler.post(tick)
    }

    private fun showResult(success: Boolean) {
        val c = createPanel()
        val accent = if (success) Color.rgb(149, 244, 255) else Color.rgb(255, 159, 166)
        text(c, if (success) "[ SCENARIO CLEARED ]" else "[ SCENARIO EXPIRED ]", 19f, accent, bold = true)
        text(c, "THE SCHOLAR'S PROOF", 26f, top = 22, bold = true)
        text(c, if (success) "Objective marked complete." else "The allotted time has elapsed.", 19f, top = 17)
        text(c, if (success) "Demo reward: 35 Coins / 50 XP" else "No reward issued.", 17f, accent, top = 20)
        text(c, "[ Prototype only — no account progress is stored yet. ]", 14f, Color.rgb(211, 229, 240), top = 19)
        buttons(c, "CLOSE", { stopSelf() }, "NEW SCENARIO", { showOffer() })
    }

    private fun dp(x: Int): Int = (resources.displayMetrics.density * x).toInt()

    override fun onDestroy() {
        clearWindow()
        super.onDestroy()
    }
}
