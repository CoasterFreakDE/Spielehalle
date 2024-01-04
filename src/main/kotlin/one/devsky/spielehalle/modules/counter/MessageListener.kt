package one.devsky.spielehalle.modules.counter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
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

        val number = event.message.contentStripped.toIntOrNull() ?: return run {
            event.message.addReaction(Emoji.fromUnicode("⁉️")).queue()
            event.message.reply("Sicher, dass das eine Zahl ist? 🤨").queue()
        }

        val lastNumber = TempStorage.readTempFileAsString("counter").toIntOrNull() ?: 0

        if (number == lastNumber + 1) {
            event.message.addReaction(Emoji.fromUnicode("✅")).queue()
            TempStorage.saveTempFile("counter", number.toString())
            TempStorage.saveTempFile(counterAuthorKey, event.author.id)
            return
        }

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