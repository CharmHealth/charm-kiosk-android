package com.charm.ehr.kiosk.presentation.web_view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.theme.MdColors

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    heading: String,
    modifier: Modifier = Modifier,
    enableJavascript: Boolean = false,
    onRequestClose: () -> Unit
) {

    var isLoading by remember { mutableStateOf(true) }

    // Keep the WebView instance across recompositions, but recreate on disposal
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    // Unified back handler
    val handleBackPress = {
        val wv = webViewRef.value
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            onRequestClose()
        }
    }

    BackHandler(enabled = true, onBack = handleBackPress)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {

        MdTopToolBar(headingText = heading, onBackClick = handleBackPress)

        Box(Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                factory = { context ->
                    WebView(context).apply {
                        webViewRef.value = this

                        // Basic security/hardening
                        settings.apply {
                            javaScriptEnabled = enableJavascript
                            domStorageEnabled = true // Often needed for modern sites
                            allowFileAccess = false
                            allowContentAccess = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                // Show an error page or UI — here we simply stop the loader
                                isLoading = false
                            }

                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                handler?.cancel()
                                isLoading = false
                            }
                        }

                        // load
                        loadUrl(url)
                    }
                },
                update = { webView ->
                    // Avoid reloading if the URL is effectively the same to prevent loops
                    val currentUrl = webView.url
                    val originalUrl = webView.originalUrl

                    // Only reload if the prop 'url' has changed and it doesn't match what's currently loaded
                    if (currentUrl != url && originalUrl != url) {
                        webView.loadUrl(url)
                    }
                },
                onRelease = { view ->
                    view.stopLoading()
                    view.loadUrl("about:blank")
                    view.webChromeClient = null
                    view.webViewClient = WebViewClient() // Reset to default client instead of null
                    view.destroy()
                    webViewRef.value = null
                }
            )
            if (isLoading) {
                MdCircularProgressBar(modifier = Modifier.align(Alignment.Center))
            }
        }

    }

}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun WebViewScreenPreview() {
    WebViewScreen(
        url = "",
        modifier = Modifier.fillMaxSize(),
        enableJavascript = true,
        onRequestClose = {},
        heading = "Privacy Policy"
    )
}