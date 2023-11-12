package com.timdeve.poche.ui.shared

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import java.net.URL

@Composable
fun linkSharer(url: URL): () -> Unit {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current
    return { context.startActivity(shareIntent) }
}

@Composable
fun linkOpener(url: URL): () -> Unit {
    val uriHandler = LocalUriHandler.current
    return { uriHandler.openUri(url.toString()) }
}

