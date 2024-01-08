package one.devsky.spielehalle.modules.stats

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.extensions.asCodeBlock

class StatisticsButtonListener: ListenerAdapter() {


    override fun onButtonInteraction(event: ButtonInteractionEvent): Unit = with(event) {
        if (componentId != "statistics.user") return

        val casinoUser = CasinoUserCache.getUser(user.id)
        val embed = EmbedBuilder()
            .setColor(0xef5777)
            .setDescription("Deine Statistiken")
            .addField("Guthaben", "$${casinoUser.money}".asCodeBlock(), true)
            .addField("Level", "${casinoUser.level}".asCodeBlock(), true)
            .addField("XP", "${casinoUser.xp} / ${casinoUser.nextLevelExp()}".asCodeBlock(), true)
            .addField("Spiele gespielt", casinoUser.gamesPlayed.asCodeBlock(), true)
            .addField("Nachrichten", casinoUser.messages.asCodeBlock(), true)
            .addField("Voicezeit", casinoUser.voiceTime.asCodeBlock(), true)
            .build()

        replyEmbeds(embed).setEphemeral(true).queue()
    }
}