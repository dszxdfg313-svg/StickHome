     package com.example.stickhome

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

class HomeActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var scroller: HorizontalScrollView
    private lateinit var dock: LinearLayout
    private var signature = ""

    private val tick = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 10_000)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply { setBackgroundColor(Color.argb(120, 0, 0, 0)) }

        clock = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 88f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        date = TextView(this).apply {
            setTextColor(Color.argb(204, 255, 255, 255))
            textSize = 24f
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(clock)
            addView(date)
        }
        root.addView(
            header,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            ).apply { setMargins(dp(64), dp(48), dp(64), 0) }
        )

        dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
        }
        scroller = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            clipChildren = false
            setPadding(dp(56), dp(24), dp(56), dp(40))
            addView(dock)
        }
        root.addView(
            scroller,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        )

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        loadApps()
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    override fun onPause() {
        handler.removeCallbacks(tick)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        scroller.smoothScrollTo(0, 0)
        dock.getChildAt(0)?.requestFocus()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // A launcher has nowhere to go back to.
    }

    private fun updateTime() {
        val now = Date()
        clock.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
        date.text = DateFormat.getDateInstance(DateFormat.FULL).format(now)
    }

    private fun loadApps() {
        val pm = packageManager
        val found = LinkedHashMap<String, ResolveInfo>()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(cat)
            for (r in pm.queryIntentActivities(query, 0)) {
                val pkg = r.activityInfo.packageName
                if (pkg != packageName && pkg !in found) found[pkg] = r
            }
        }
        val apps = found.values.sortedBy { it.loadLabel(pm).toString().lowercase() }
        val sig = apps.joinToString("|") { it.activityInfo.packageName }
        if (sig == signature) return
        signature = sig

        dock.removeAllViews()
        apps.forEach { dock.addView(tile(it)) }
        dock.getChildAt(0)?.requestFocus()
    }

    private fun tile(r: ResolveInfo): View {
        val pm = packageManager
        val pkg = r.activityInfo.packageName
        val bg = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(Color.TRANSPARENT)
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            isFocusable = true
            isClickable = true
            background = bg
            setPadding(dp(14), dp(14), dp(14), dp(10))
            layoutParams = LinearLayout.LayoutParams(dp(132), ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { marginEnd = dp(12) }

            addView(ImageView(context).apply {
                setImageDrawable(r.loadIcon(pm))
                layoutParams = LinearLayout.LayoutParams(dp(72), dp(72))
            })
            addView(TextView(context).apply {
                text = r.loadLabel(pm)
                setTextColor(Color.WHITE)
                textSize = 15f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, 0)
            })

            setOnFocusChangeListener { v, focused ->
                bg.setColor(if (focused) Color.argb(60, 255, 255, 255) else Color.TRANSPARENT)
                val s = if (focused) 1.12f else 1f
                v.animate().scaleX(s).scaleY(s).setDuration(120).start()
            }
            setOnClickListener { launch(pkg) }
        }
    }

    private fun launch(pkg: String) {
        val pm = packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(pkg)
            ?: pm.getLaunchIntentForPackage(pkg)
            ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
