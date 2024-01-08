package one.devsky.spielehalle.modules.stats

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import kotlin.time.Duration.Companion.minutes

class MessageListener : ListenerAdapter() {

    private val coolDowns = mutableMapOf<String, Calendar>()

    override fun onMessageReceived(event: MessageReceivedEvent): Unit = with(event) {
        if (author.isBot) return

        if (coolDowns.containsKey(author.id)) {
            val time = coolDowns[author.id] ?: return
            if (time.isAfter(Calendar.now())) return
        }
        coolDowns[author.id] = Calendar.now() + 1.minutes

        val casinoUser = CasinoUserCache.getUser(author.id)
        casinoUser.addEXP(1)
        CasinoUserCache.saveUser(casinoUser.copy(messages = casinoUser.messages + 1))
    }
}