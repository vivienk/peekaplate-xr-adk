package com.peekaplate.market

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream
import java.io.IOException

/** Hosts the original market using resources shipped inside the APK. */
class GameActivity : Activity() {
    private lateinit var game: WebView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        game = WebView(this)
        game.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }
        game.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (request.isForMainFrame && uri.scheme == "https" && uri.host == HOST &&
                    uri.path in setOf("/portion/noka-steak-ramen", "/portion/thai-chicken-bowl")) {
                    startActivity(Intent(this@GameActivity, LaunchActivity::class.java)
                        .putExtra("model", uri.lastPathSegment + ".glb"))
                }
                return true
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                val uri = request.url
                val path = uri.path.orEmpty().removePrefix("/")
                if (uri.scheme != "https" || uri.host != HOST || ".." in path.split("/")) return missing()
                val extension = path.substringAfterLast('.')
                val mime = when (extension) {
                    "js", "mjs" -> "application/javascript"
                    "glb" -> "model/gltf-binary"
                    "wasm" -> "application/wasm"
                    else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
                }
                return try {
                    WebResourceResponse(mime, null, assets.open("game/$path"))
                } catch (_: IOException) {
                    missing()
                }
            }
        }
        setContentView(game)
        game.loadUrl("https://$HOST/index.html")
    }

    override fun onPause() { game.onPause(); super.onPause() }
    override fun onResume() { super.onResume(); game.onResume() }
    override fun onDestroy() { game.destroy(); super.onDestroy() }

    private fun missing() = WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
        emptyMap(), ByteArrayInputStream(byteArrayOf()))

    private companion object { const val HOST = "appassets.androidplatform.net" }
}
