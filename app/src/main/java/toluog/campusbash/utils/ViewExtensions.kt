package toluog.campusbash.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.jetbrains.anko.textColor
import toluog.campusbash.R

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

fun Context.alertDialog(msg: String, title: String? = null): AndroidAlertBuilder {
    return AndroidAlertBuilder(this).apply {
        if (title != null) {
            this.title = title
        }
        this.message = msg
    }
}

class AndroidAlertBuilder(ctx: Context) {
    val builder = AlertDialog.Builder(ctx, R.style.AlertDialogTheme)
    var icon: Drawable? = null
        set(value) {
            field = value
            builder.setIcon(field)
        }

    var message: CharSequence = ""
        set(value) {
            field = value
            builder.setMessage(field)
        }

    var title: CharSequence = ""
        set(value) {
            field = value
            builder.setTitle(field)
        }

    fun build() = builder.create()

    fun <T> items(items: List<T>, onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, items[which], which)
        }
    }

    fun items(items: List<CharSequence>, onItemSelected: (dialog: DialogInterface, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, which)
        }
    }

    fun negativeButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    fun neutralPressed(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    fun onCancelled(handler: (dialog: DialogInterface) -> Unit) {
        builder.setOnCancelListener(handler)
    }

    fun onKeyPressed(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean) {
        builder.setOnKeyListener(handler)
    }

    fun positiveButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    fun show() = builder.show()

}