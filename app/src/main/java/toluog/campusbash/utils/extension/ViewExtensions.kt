package toluog.campusbash.utils.extension

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar

inline val Fragment.act: Activity
    get() = this.requireActivity()

inline val Fragment.actCompat: AppCompatActivity
    get() = this.requireActivity() as AppCompatActivity

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
    this.setTextColor(ContextCompat.getColor(context, color))
}

fun Context.alertDialog(msg: String? = null, title: String? = null): AndroidAlertBuilder {
    return AndroidAlertBuilder(this).apply {
        if (title != null) {
            this.title = title
        }
        if (msg != null) {
            this.message = msg
        }
    }
}

fun View.longSnackbar(message: Int) = Snackbar
        .make(this, this.context.getString(message), Snackbar.LENGTH_LONG).show()

fun View.snackbar(message: Int) = Snackbar
        .make(this, this.context.getString(message), Snackbar.LENGTH_SHORT).show()

fun View.indefiniteSnackbar(message: Int) = Snackbar
        .make(this, this.context.getString(message), Snackbar.LENGTH_INDEFINITE).show()


fun Context.toast(resource: Int) = Toast.makeText(this, this.getString(resource), Toast.LENGTH_SHORT)

fun Context.longToast(resource: Int) = Toast.makeText(this, this.getString(resource), Toast.LENGTH_LONG)

fun Context.progressDialog(
        message: CharSequence? = null,
        title: CharSequence? = null,
        init: (ProgressDialog.() -> Unit)? = null
) = progressDialog(false, message, title, init)

fun Context.indeterminateProgressDialog(
        message: Int? = null,
        title: Int? = null,
        init: (ProgressDialog.() -> Unit)? = null
) = progressDialog(true, message?.let { getString(it) }, title?.let { getString(it) }, init)

private fun Context.progressDialog(
        indeterminate: Boolean,
        message: CharSequence? = null,
        title: CharSequence? = null,
        init: (ProgressDialog.() -> Unit)? = null
) = ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT).apply {
    isIndeterminate = indeterminate
    if (!indeterminate) setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    if (message != null) setMessage(message)
    if (title != null) setTitle(title)
    if (init != null) init()
    show()
}


class AndroidAlertBuilder(ctx: Context) {
    val builder = AlertDialog.Builder(ctx)
    var icon: Drawable? = null
        set(value) {
            field = value
            builder.setIcon(field)
        }

    var message: CharSequence? = null
        set(value) {
            field = value
            builder.setMessage(field)
        }

    var title: CharSequence? = null
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

    fun show(): AlertDialog {
        build()
        return builder.show()
    }

}