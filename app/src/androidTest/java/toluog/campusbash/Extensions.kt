package toluog.campusbash

import android.arch.lifecycle.LiveData

fun <T> LiveData<T>.data(): T? {
    return LiveDataTestUtil.getValue(this)
}