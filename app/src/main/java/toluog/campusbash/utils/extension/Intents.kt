package toluog.campusbash.utils.extension

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

inline fun <reified T> Context.intentFor(bundle: Bundle) = Intent(this, T::class.java)
        .apply {
            putExtras(bundle)
        }

inline fun <reified T> Fragment.intentFor(bundle: Bundle) = Intent(this.act, T::class.java)
        .apply {
            putExtras(bundle)
        }

inline fun <reified T> Context.intentFor() = Intent(this, T::class.java)

inline fun <reified T> Fragment.intentFor() = Intent(this.act, T::class.java)