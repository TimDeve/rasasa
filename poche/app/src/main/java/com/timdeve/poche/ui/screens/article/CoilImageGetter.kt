package com.timdeve.poche.ui.screens.article

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import androidx.compose.ui.unit.IntSize
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest

// Based on implementation from https://stackoverflow.com/a/75636129
open class CoilImageGetter(
    private val textView: TextView,
    private val imageLoader: ImageLoader = Coil.imageLoader(textView.context),
    private val sourceModifier: ((source: String) -> String)? = null,
    private val maxSize: IntSize? = null,
) : Html.ImageGetter {

    override fun getDrawable(source: String): Drawable {
        val finalSource = sourceModifier?.invoke(source) ?: source

        val drawablePlaceholder = DrawablePlaceHolder(maxSize)
        imageLoader.enqueue(ImageRequest.Builder(textView.context).data(finalSource).apply {
            target { drawable ->
                drawablePlaceholder.updateDrawable(drawable)
                // invalidating the drawable doesn't seem to be enough...
                textView.text = textView.text
            }
        }.build())
        // Since this loads async, we return a "blank" drawable, which we update
        // later
        return drawablePlaceholder
    }

    @Suppress("DEPRECATION")
    private class DrawablePlaceHolder(private val maxSize: IntSize?) : BitmapDrawable() {

        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.draw(canvas)
        }

        fun updateDrawable(drawable: Drawable) {
            this.drawable = drawable
            // Calculate ratio to display at full width
            val ratio =
                (maxSize?.width ?: drawable.intrinsicWidth).toDouble() / drawable.intrinsicWidth
            val width = (drawable.intrinsicWidth * ratio).toInt()
            val height = (drawable.intrinsicHeight * ratio).toInt()
            drawable.setBounds(0, 0, width, height)
            setBounds(0, 0, width, height)
        }
    }
}