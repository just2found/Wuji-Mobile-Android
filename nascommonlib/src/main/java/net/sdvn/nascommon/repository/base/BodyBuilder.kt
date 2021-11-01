package net.sdvn.nascommon.repository.base

class BodyBuilder(val method: String) {

    private var session: String? = null
    private var params: Map<String, Any>? = null

    fun addSession(session: String): BodyBuilder {
        this.session = session
        return this
    }

    fun addParams(params: Map<String, Any>): BodyBuilder {
        this.params = params
        return this
    }

    fun build(): Map<String, Any> {
        val body = mutableMapOf<String, Any>()
        body["method"] = method
        if (!session.isNullOrEmpty()) {
            body["session"] = session!!
        }
        if (!params.isNullOrEmpty()) {
            body["params"] = params!!
        }
        return body
    }
}