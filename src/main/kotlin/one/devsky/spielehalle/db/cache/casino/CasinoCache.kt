package one.devsky.spielehalle.db.cache.casino

import net.dv8tion.jda.api.EmbedBuilder
import one.devsky.spielehalle.Spielehalle
import one.devsky.spielehalle.db.model.enums.Game
import one.devsky.spielehalle.db.model.enums.LogType
import one.devsky.spielehalle.extensions.asCodeBlock
import one.devsky.spielehalle.extensions.plus
import one.devsky.spielehalle.utils.Environment
import one.devsky.spielehalle.utils.TempStorage
import one.devsky.spielehalle.utils.sendLogEmbed
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object CasinoCache {

    private val cacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private var money: Double = Environment.getEnv("START_MONEY_CASINO")?.toDouble() ?: 1000000.0

    init {
        val tempMoney = TempStorage.readTempFileAsString("casino.money")
        if (tempMoney.isNotEmpty()) {
            money = tempMoney.toDoubleOrNull() ?: error("Invalid money value in temp file.")
        }
    }

    /**
     * Retrieves the current amount of money in the casino.
     *
     * @return The current amount of money in the casino.
     */
    fun getMoney(): Double {
        cacheLock.readLock().lock()
        val money = money
        cacheLock.readLock().unlock()
        return money
    }

    /**
     * Modifies the amount of money by the given value and saves the updated value to a temporary file.
     *
     * @param amount The amount to be added to the current money value.
     */
    fun modifyMoney(amount: Double, game: Game) {
        cacheLock.writeLock().lock()
        money += amount
        TempStorage.saveTempFile("casino.money", money.toString())
        cacheLock.writeLock().unlock()

        val color = if (amount > 0) LogType.INFO.color else LogType.ERROR.color

        sendLogEmbed(
            EmbedBuilder()
                .setColor(game.color + color)
                .setDescription("Das Casinokonto wurde modifiziert.")
                .addField("Amount", "$$amount".asCodeBlock(), true)
                .addField("New amount", "$$money".asCodeBlock(), true)
                .addField("Game",
                    game.name.lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    .asCodeBlock(), true)
                .setFooter("Spielehalle | Logs", Spielehalle.instance.jda.selfUser.avatarUrl),
            logType = LogType.CUSTOM
        )?.queue()
    }
}