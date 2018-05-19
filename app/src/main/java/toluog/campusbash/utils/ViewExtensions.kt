package toluog.campusbash.utils

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.jetbrains.anko.textColor

fun ImageView.loadImage(url: String?) {
    Glide.with(context).load(url).into(this)
}

fun ImageView.loadImage(uri: Uri?) {
    Glide.with(context).load(uri).into(this)
}

fun TextView.updateTextSelector(message: String, color: Int) {
    text = message
    textColor = context.resources.getColor(color)
}