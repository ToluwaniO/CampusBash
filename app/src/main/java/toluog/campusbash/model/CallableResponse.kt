package toluog.campusbash.model

data class CallableResponse(var message: String = "", var code: Int = 400)

fun CallableResponse.fromMap(map: Map<String, Any?>): CallableResponse {
    return this.apply {
        message = map["message"] as String? ?: ""
        code = map["code"] as Int? ?: 400
    }
}