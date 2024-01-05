package one.devsky.spielehalle.modules.setup

import dev.fruxz.ascend.extension.getResource
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.FileUpload

class StatsSetup(private val channel: TextChannel) : PanelCreation {

    override fun createPanel() {
        val embed1 = EmbedBuilder()
            .setColor(0xef5777)
            .setImage("attachment://statistics.png")
            .build()

        val embed2 = EmbedBuilder()
            .setColor(0xef5777)
            .setTitle("Statistiken")
            .setDescription("Hier kannst du dir die Statistiken der Spielehalle ansehen.")
            .setFooter("Spielehalle | Statistiken", channel.guild.iconUrl)
            .build()

        channel.sendMessageEmbeds(embed1, embed2)
            .setActionRow(
                Button.secondary("statistics.user", "Deine Statistiken")
                    .withEmoji(Emoji.fromFormatted("📊")),
            )
            .addFiles(FileUpload.fromData(getResource("assets/games/statistics.png"), "statistics.png"))
            .queue()
    }
}