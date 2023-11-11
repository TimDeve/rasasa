package com.timdeve.poche.ui.shared

import android.text.method.LinkMovementMethod
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.google.android.material.textview.MaterialTextView

@Composable
fun HtmlContent(content: String, modifier: Modifier = Modifier) {
    val textColor = colorScheme.onSurface
    val linkColor = colorScheme.tertiary
    var size by remember { mutableStateOf(IntSize.Zero) }

    AndroidView(
        modifier = modifier.onGloballyPositioned { size = it.size },
        factory = {
            MaterialTextView(it).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor.toArgb())
                setLinkTextColor(linkColor.toArgb())
            }
        },
        update = {
            it.text = HtmlCompat.fromHtml(
                content,
                HtmlCompat.FROM_HTML_MODE_LEGACY,
                CoilImageGetter(it, maxSize = size),
                null
            )
        }
    )
}