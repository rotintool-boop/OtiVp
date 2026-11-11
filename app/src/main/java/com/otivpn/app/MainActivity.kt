package com.otivpn.app

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.ByteArrayInputStream

class MainActivity : ComponentActivity() {
    private var config: Config? = null
    private var tunnel: Tunnel? = null
    private lateinit var status: TextView
    private lateinit var backend: GoBackend

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) try {
            val data = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("فایل خوانده نشد")
            config = Config.parse(ByteArrayInputStream(data))
            status.text = "کانفیگ WireGuard بارگذاری شد"
        } catch (e: Exception) {
            status.text = "فایل کانفیگ معتبر نیست"
            Toast.makeText(this, e.message ?: "خطا", Toast.LENGTH_LONG).show()
        }
    }

    private val permission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) connectNow() else status.text = "مجوز VPN داده نشد"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backend = GoBackend(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(36, 48, 36, 36) }
        layout.addView(TextView(this).apply { text = "OtiVPN"; textSize = 30f; textAlignment = TextView.TEXT_ALIGNMENT_CENTER })
        status = TextView(this).apply { text = "برای شروع فایل کانفیگ WireGuard را وارد کنید"; textSize = 16f; setPadding(0, 32, 0, 32) }
        layout.addView(status)
        layout.addView(Button(this).apply { text = "انتخاب فایل کانفیگ"; setOnClickListener { picker.launch(arrayOf("*/*")) } })
        layout.addView(Button(this).apply { text = "اتصال VPN"; setOnClickListener { requestConnect() } })
        layout.addView(Button(this).apply { text = "قطع اتصال"; setOnClickListener { disconnect() } })
        setContentView(layout)
    }

    private fun requestConnect() {
        if (config == null) { status.text = "ابتدا فایل کانفیگ را انتخاب کنید"; return }
        val intent = VpnService.prepare(this)
        if (intent != null) permission.launch(intent) else connectNow()
    }

    private fun connectNow() {
        try {
            val t = tunnel ?: object : Tunnel {
                override fun getName() = "OtiVPN"
                override fun onStateChange(newState: Tunnel.State) {
                    runOnUiThread { status.text = if (newState == Tunnel.State.UP) "متصل" else "قطع" }
                }
            }.also { tunnel = it }
            backend.setState(t, Tunnel.State.UP, config!!)
            status.text = "در حال اتصال..."
        } catch (e: Exception) {
            status.text = "اتصال ناموفق بود"
            Toast.makeText(this, e.message ?: "خطای اتصال", Toast.LENGTH_LONG).show()
        }
    }

    private fun disconnect() {
        try { tunnel?.let { backend.setState(it, Tunnel.State.DOWN, null) }; status.text = "اتصال قطع شد" }
        catch (e: Exception) { status.text = "خطا در قطع اتصال" }
    }
}
