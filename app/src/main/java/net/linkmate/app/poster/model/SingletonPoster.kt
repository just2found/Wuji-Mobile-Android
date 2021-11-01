package net.linkmate.app.poster.model

class SingletonPoster private constructor() {

    var token: String? = null

    companion object {
        private var instance: SingletonPoster? = null
            get() {
                if (field == null) {
                    field = SingletonPoster()
                }
                return field
            }
        fun get(): SingletonPoster{
            return instance!!
        }
    }
}