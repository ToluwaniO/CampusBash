package toluog.campusbash.utils

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.jetbrains.anko.textColor

fun ImageView.loadImage(url: Any?) {
    Glide.with(context).load(url).into(this)
}

fun ImageView.lazyLoadImage(act: AppCompatActivity, url: Any?) {
    Glide.with(context)
            .load(url).listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            Log.e(act.localClassName, e?.toString())
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            act.supportStartPostponedEnterTransition()
            return false
        }

    }).into(this)
}

fun TextView.updateTextSelector(message: String, color: Int) {
    text = message
    textColor = context.resources.getColor(color)
}