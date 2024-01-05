package one.devsky.spielehalle.db.cache.users

import one.devsky.spielehalle.db.functions.users.loadCasinoUserDB
import one.devsky.spielehalle.db.functions.users.saveCasinoUserDB
import one.devsky.spielehalle.db.model.users.CasinoUser
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object CasinoUserCache {

    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private var cache: Map<String, CasinoUser> = emptyMap()

    /**
     * Retrieves a CasinoUser object based on the provided clientId.
     *
     * @param clientId The unique identifier of the user.
     * @return The CasinoUser object associated with the provided clientId. If the user is not found in the cache,
     * a new CasinoUser is loaded from the database or created with default property values if not found in the database.
     */
    fun getUser(clientId: String): CasinoUser {
        cacheLock.readLock().lock()
        val user = cache[clientId]
        cacheLock.readLock().unlock()

        if (user == null) {
            val newUser = loadCasinoUserDB(clientId)
            saveUser(newUser, false)
            return newUser
        }

        return user
    }

    /**
     * Saves a CasinoUser object to the cache.
     *
     * @param user The CasinoUser object to be saved.
     */
    fun saveUser(user: CasinoUser, saveToDB: Boolean = true) {
        cacheLock.writeLock().lock()
        cache = cache + (user.clientId to user)
        cacheLock.writeLock().unlock()

        if (saveToDB) {
            saveCasinoUserDB(user)
        }
    }
}