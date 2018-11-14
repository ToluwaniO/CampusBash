package toluog.campusbash

import androidx.lifecycle.LiveData

fun <T> LiveData<T>.data(): T? {
    return LiveDataTestUtil.getValue(this)
}