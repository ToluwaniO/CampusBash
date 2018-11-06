package toluog.campusbash.data.network

sealed class ServerResponseState {
    data class Success<T>(val data: T): ServerResponseState()
    object Loading: ServerResponseState()
    data class Error(val exception: Exception?): ServerResponseState()
}