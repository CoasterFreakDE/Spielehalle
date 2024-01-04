package one.devsky.spielehalle.modules.status

import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity

class StatusChanger(private val jda: JDA) {

    private val job: Job

    private val botStatuses = listOf(
        BotStatus("Blackjack oder Poker?", listOf("🤔", "🥣", "🤷")),
        BotStatus("Wo bleibt mein Kaffee?", listOf("☕️", "🥺", "🤔")),
        BotStatus("Ich bin arm.", listOf("🥫")),
        BotStatus("HAPPY PRIDE", listOf("🏳‍🌈", "🌈", "🦄", "🌸")),
        BotStatus("Guten Morgen/Tag/Abend!", listOf("👋", "👋🏻", "👋🏼", "👋🏽", "👋🏾", "👋🏿")),
    )

    init {
        job = CoroutineScope(Dispatchers.Default).launch {
            start()
        }
    }

    private suspend fun start() {
        doInfinity("0 /5 * * *") {
            val status = botStatuses.random()
            val emoji = status.emojis.randomOrNull() ?: ""
            jda.presence.setPresence(Activity.customStatus("$emoji ${status.status}"), false)
        }
    }

    fun stop() {
        job.cancel()
    }
}