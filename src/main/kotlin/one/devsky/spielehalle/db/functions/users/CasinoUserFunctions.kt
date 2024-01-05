package one.devsky.spielehalle.db.functions.users

import one.devsky.spielehalle.db.model.users.CasinoUser
import one.devsky.spielehalle.db.tables.users.CasinoUserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Maps a database row to a CasinoUser object.
 *
 * @param row The database row to be mapped.
 * @return The CasinoUser object representing the data in the row.
 */
fun mapToCasinoUser(row: ResultRow): CasinoUser = CasinoUser(
    clientId = row[CasinoUserTable.clientId],
    money = row[CasinoUserTable.money],
    xp = row[CasinoUserTable.xp],
    messages = row[CasinoUserTable.messages],
    voiceTime = row[CasinoUserTable.voiceTime].toDuration(DurationUnit.MILLISECONDS),
    winnings = row[CasinoUserTable.winnings],
    losses = row[CasinoUserTable.losses],
    gamesPlayed = row[CasinoUserTable.gamesPlayed]
)

/**
 * Loads a CasinoUser from the database based on the provided clientId. If a matching CasinoUser is found in the
 * database, it is returned. Otherwise, a new CasinoUser object is created with the provided clientId and default
 * property values.
 *
 * @param clientId The unique identifier of the user.
 * @return The loaded CasinoUser object from the database if found, or a new CasinoUser object with the provided
 * clientId and default property values if not found.
 */
fun loadCasinoUserDB(clientId: String): CasinoUser = transaction {
    CasinoUserTable.select {
        CasinoUserTable.clientId eq clientId
    }.firstOrNull()?.let(::mapToCasinoUser) ?: CasinoUser(clientId)
}

/**
 * Saves a CasinoUser's data to the database.
 *
 * @param user The CasinoUser object containing the user's data to be saved.
 */
fun saveCasinoUserDB(user: CasinoUser) = transaction {
    CasinoUserTable.replace {
        it[clientId] = user.clientId
        it[money] = user.money
        it[xp] = user.xp
        it[messages] = user.messages
        it[voiceTime] = user.voiceTime.inWholeMilliseconds
        it[winnings] = user.winnings
        it[losses] = user.losses
        it[gamesPlayed] = user.gamesPlayed
    }
}