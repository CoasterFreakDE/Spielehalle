package one.devsky.spielehalle.modules.coinflip

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.extensions.asCodeBlock
import one.devsky.spielehalle.extensions.toInt
import kotlin.time.Duration.Companion.seconds

class CoinflipButtonListener: ListenerAdapter() {

    private val cooldown = mutableMapOf<String, Calendar>()


    override fun onButtonInteraction(event: ButtonInteractionEvent): Unit = with(event) {
        if (!componentId.startsWith("coinflip.")) return
        val head = componentId.endsWith("head")

        if (cooldown.containsKey(user.id)) {
            val time = cooldown[user.id] ?: return
            if (time.isAfter(Calendar.now())) {
                reply("Du musst noch ${time.durationFromNow()} warten, bevor du wieder spielen kannst!")
                    .setEphemeral(true).queue()
                return
            }
        }
        cooldown[user.id] = Calendar.now() + 5.seconds


        var casinoUser = CasinoUserCache.getUser(user.id)
        if (casinoUser.money < 5) {
            reply("Du hast leider nicht genug Guthaben zum spielen!").setEphemeral(true).queue()
            return
        }

        val result = (0..1).random() == head.toInt()

        val embed = EmbedBuilder()
            .setColor(0xef5777)
            .setTitle("Coinflip")
            .setDescription("Du hast ${if (result) "gewonnen" else "verloren"}!")
            .addField("Gewinn", "$${if (result) 5 else -5}".asCodeBlock(), true)
            .addField("Guthaben", "$${casinoUser.money + if (result) 5 else -5}".asCodeBlock(), true)
            .build()

        casinoUser = casinoUser.copy(
            money = casinoUser.money + if (result) 5 else -5,
            gamesPlayed = casinoUser.gamesPlayed + 1,
            winnings = casinoUser.winnings + if (result) 5 else 0,
            losses = casinoUser.losses + if (result) 0 else 5,
            xp = casinoUser.xp + if (result) 3 else 1
        )
        CasinoUserCache.saveUser(casinoUser)

        replyEmbeds(embed).setEphemeral(true).queue()
    }
}