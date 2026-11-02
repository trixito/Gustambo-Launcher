package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.movtery.zalithlauncher.game.discord.DiscordRpcStore

/**
 * Gustambo: login de Discord mediante WebView (la contraseña nunca pasa por el launcher).
 * Al detectar la sesión iniciada, guarda el token cifrado en el dispositivo y se cierra.
 */
class DiscordLoginActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val handler = Handler(Looper.getMainLooper())
    private var finished = false
    private var attempts = 0

    // Intenta leer el token de la sesión ya iniciada en la página
    private val extractScript = """
        (function() {
          try {
            var f = document.body.appendChild(document.createElement('iframe'));
            var t = f.contentWindow.localStorage.getItem('token');
            f.remove();
            if (t) return t.replace(/"/g, '');
          } catch (e) {}
          try {
            var m = [];
            webpackChunkdiscord_app.push([[Math.random()], {}, function(r) {
              for (var k in r.c) m.push(r.c[k]);
            }]);
            var mod = m.find(function(x) {
              return x && x.exports && x.exports.default && x.exports.default.getToken !== undefined;
            });
            if (mod) return mod.exports.default.getToken();
          } catch (e) {}
          return '';
        })()
    """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Empezar siempre desde una sesión limpia
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                    checkLoggedIn(url)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    checkLoggedIn(url)
                }
            }
        }
        setContentView(webView)
        webView.loadUrl("https://discord.com/login")
    }

    private fun checkLoggedIn(url: String?) {
        if (finished || url == null) return
        if (url.contains("discord.com/channels") || url.contains("discord.com/app")) {
            attempts = 0
            tryExtract()
        }
    }

    private fun tryExtract() {
        if (finished) return
        webView.evaluateJavascript(extractScript) { raw ->
            val token = raw?.trim('"')?.takeIf { it.length > 20 && it != "null" }
            if (token != null) {
                finished = true
                DiscordRpcStore.saveToken(this, token)
                setResult(RESULT_OK)
                CookieManager.getInstance().removeAllCookies(null)
                WebStorage.getInstance().deleteAllData()
                finish()
            } else if (attempts++ < 10) {
                // La página aún carga; reintentar
                handler.postDelayed({ tryExtract() }, 1000)
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }
}
