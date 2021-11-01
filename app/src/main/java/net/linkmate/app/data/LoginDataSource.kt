import libs.source.common.livedata.Resource
import net.linkmate.app.data.model.LoggedInUser

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Resource<LoggedInUser> {
        return Resource.success(LoggedInUser(username, password))
    }

    fun logout() {
    }
}

