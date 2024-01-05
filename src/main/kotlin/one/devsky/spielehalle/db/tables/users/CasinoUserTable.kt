package one.devsky.spielehalle.db.tables.users

import one.devsky.spielehalle.utils.Environment
import org.jetbrains.exposed.sql.Table

/**
 * Represents a database table for storing information related to casino users.
 * Inherits from the `Table` class.
 *
 * @property clientId The client ID of the user, stored as a varchar.
 * @property money The amount of money the user has, stored as a double. It is set to a default value based on the 'START_MONEY' environment variable, or 0.0 if the variable is not
 * found.
 * @property xp The experience points (XP) earned by the user, stored as an integer. It is set to a default value of 0.
 * @property messages The number of messages sent by the user, stored as an integer. It is set to a default value of 0.
 * @property voiceTime The total time spent in voice chat by the user, stored as a long. It is set to a default value of 0.
 */
object CasinoUserTable : Table("casino_users") {
    val clientId = varchar("client_id", 32)
    val money = double("money").default(Environment.getEnv("START_MONEY")?.toDouble() ?: 0.0)
    val xp = integer("xp").default(0)
    val messages = integer("messages").default(0)
    val voiceTime = long("voice_time").default(0)
    val winnings = double("winnings").default(0.0)
    val losses = double("losses").default(0.0)
    val gamesPlayed = integer("games_played").default(0)

    override val primaryKey = PrimaryKey(clientId)
}