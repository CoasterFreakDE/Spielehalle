package one.devsky.spielehalle.modules.counter

import dev.fruxz.ascend.extension.data.randomInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.spielehalle.db.cache.casino.CasinoCache
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.db.model.enums.Game
import one.devsky.spielehalle.openai.OpenAIHandler
import one.devsky.spielehalle.utils.Environment
import one.devsky.spielehalle.utils.TempStorage
import java.util.concurrent.TimeUnit

/**
 * Listener für den Counter Channel
 *
 * Kanal wird mit CHANNEL_COUNTER in der .env definiert
 *
 * @property counterAuthorKey Name der Temp File, die den Autor der letzten Nachricht speichert
 */
@Suppress("unused")
class MessageListener : ListenerAdapter() {

    private val counterAuthorKey = "counter-author"

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (event.channel.id != Environment.getEnv("CHANNEL_COUNTER")) return

        if (event.message.author.id == TempStorage.readTempFileAsString(counterAuthorKey) && event.message.author.id != Environment.getEnv("DEV_USER")) {
            event.message.addReaction(Emoji.fromUnicode("🛑")).queue()

            event.message.reply("Du kannst nicht zwei mal hintereinander schreiben 😔").queue { message ->
                message.delete().queueAfter(5, TimeUnit.SECONDS)
            }
            event.message.delete().queueAfter(5, TimeUnit.SECONDS)
            return
        }

        var casinoUser = CasinoUserCache.getUser(event.author.id)
        if (casinoUser.money < 1.0) {
            event.message.addReaction(Emoji.fromUnicode("🛑")).queue()

            event.message.reply("Du hast leider nicht genug Guthaben zum mitmachen!").queue { message ->
                message.delete().queueAfter(5, TimeUnit.SECONDS)
            }
            event.message.delete().queueAfter(5, TimeUnit.SECONDS)
            return
        }
        casinoUser.addEXP(1)
        casinoUser = casinoUser.copy(money = casinoUser.money - 1)
        CasinoCache.modifyMoney(1.0, Game.COUNTER, event.author)


        val number = event.message.contentStripped.toIntOrNull() ?: return run {
            event.message.addReaction(Emoji.fromUnicode("⁉️")).queue()
            event.message.reply("Sicher, dass das eine Zahl ist? 🤨").queue()
            CasinoUserCache.saveUser(casinoUser.copy(losses = casinoUser.losses + 1))
        }

        val lastNumber = TempStorage.readTempFileAsString("counter").toIntOrNull() ?: 0

        if (number == lastNumber + 1) {
            event.message.addReaction(Emoji.fromUnicode("✅")).queue()
            TempStorage.saveTempFile("counter", number.toString())
            TempStorage.saveTempFile(counterAuthorKey, event.author.id)

            if (number % 10 == 0) {
                casinoUser.addEXP(10)
                CasinoUserCache.saveUser(casinoUser.copy(money = casinoUser.money + 5, winnings = casinoUser.winnings + 5))
                CasinoCache.modifyMoney(-5.0, Game.COUNTER, event.author)
                event.message.addReaction(Emoji.fromUnicode("💰")).queue()
                event.message.reply("Du hast eine runde Zahl erreicht! Du bekommst $5 und 10 XP!").queue { message ->
                    message.delete().queueAfter(5, TimeUnit.SECONDS)
                }
                return
            }

            if (number % randomInt(5.. 50) == 0) {
                casinoUser.addEXP(20)
                CasinoUserCache.saveUser(casinoUser.copy(money = casinoUser.money + 25, winnings = casinoUser.winnings + 25))
                CasinoCache.modifyMoney(-25.0, Game.COUNTER, event.author)
                event.message.addReaction(Emoji.fromUnicode("⚱️")).queue()
                event.message.reply("Herzlichen Glückwunsch. Du hast ein Paket gefunden. Du bekommst $25 und 20 XP!").queue { message ->
                    message.delete().queueAfter(5, TimeUnit.SECONDS)
                }
                return
            }

            CasinoUserCache.saveUser(casinoUser.copy(losses = casinoUser.losses + 1))
            return
        }

        CasinoUserCache.saveUser(casinoUser.copy(losses = casinoUser.losses + 1))
        event.message.addReaction(Emoji.fromUnicode("❌")).queue()

        TempStorage.saveTempFile("counter", "0")
        TempStorage.saveTempFile(counterAuthorKey, "0")

        CoroutineScope(Dispatchers.Default).launch {
            val message = OpenAIHandler.getSingleAnswer(
                "Schreibe eine super kurze Nachricht (10-20 Wörter), das der Counter wieder auf 1 gesetzt wurde.",
                "Der User ${event.author.effectiveName} hat eine falsche Zahl (${event.message.contentStripped}) eingegeben. Die richtige Zahl wäre ${lastNumber + 1} gewesen. Necke den User.",
                200
            )
            event.message.reply(message).queue()
        }

    }
}