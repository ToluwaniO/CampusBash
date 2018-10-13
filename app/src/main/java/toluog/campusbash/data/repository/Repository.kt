package toluog.campusbash.data.repository

import kotlinx.coroutines.CoroutineScope

abstract class Repository: CoroutineScope {
    abstract fun clear()
}