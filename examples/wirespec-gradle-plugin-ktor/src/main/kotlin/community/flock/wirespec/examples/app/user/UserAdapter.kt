package community.flock.wirespec.examples.app.user

interface HasUserAdapter {
    val userAdapter: UserAdapter
}

interface UserAdapter {
    fun getAllUsers(): List<User>

    fun getUserByName(name: String): User

    fun saveUser(user: User): User

    fun deleteUserByName(name: String): User
}
