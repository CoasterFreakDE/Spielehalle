package one.devsky.spielehalle.db.model.users

import one.devsky.spielehalle.utils.Environment
import kotlin.time.Duration

/**
 * Represents a user in a casino.
 *
 * @property clientId The unique identifier of the user.
 * @property money The amount of money the user has. Default value is retrieved from the environment variable START_MONEY.
 * @property xp The experience points of the user. Default value is 0.
 * @property messages The number of messages sent by the user. Default value is 0.
 * @property voiceTime The duration of voice time for the user. Default value is zero duration.
 */
data class CasinoUser(
    val clientId: String,
    val money: Double = Environment.getEnv("START_MONEY")?.toDouble() ?: 0.0,
    val xp: Int = 0,
    val messages: Int = 0,
    val voiceTime: Duration = Duration.ZERO,
    val winnings: Double = 0.0,
    val losses: Double = 0.0,
    val gamesPlayed: Int = 0,
)
