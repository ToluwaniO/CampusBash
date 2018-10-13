package toluog.campusbash.data.datasource

import kotlinx.coroutines.CoroutineScope

abstract class DataSource: CoroutineScope {
    abstract fun clear()
}