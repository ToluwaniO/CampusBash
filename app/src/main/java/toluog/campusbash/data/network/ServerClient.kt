package toluog.campusbash.data.network

interface ServerClient<T> {
    fun createClient(): T
}