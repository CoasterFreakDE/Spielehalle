package one.devsky.spielehalle.modules.setup

import dev.fruxz.ascend.extension.getResource
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.FileUpload

class CoinflipSetup(private val channel: TextChannel) : PanelCreation {

    override fun createPanel() {
        val embed1 = EmbedBuilder()
            .setColor(0xef5777)
            .setImage("attachment://coinflip.png")
            .build()

        val embed2 = EmbedBuilder()
            .setColor(0xef5777)
            .setTitle("Coinflip")
            .setDescription("Werfe eine Münze um $5.")
            .setFooter("Spielehalle | Coinflip", channel.guild.iconUrl)
            .build()

        channel.sendMessageEmbeds(embed1, embed2)
            .setActionRow(
                Button.secondary("coinflip.head", "Kopf ($5 einsetzen)")
                    .withEmoji(Emoji.fromFormatted("🫨")),
                Button.secondary("coinflip.tail", "Zahl ($5 einsetzen)")
                    .withEmoji(Emoji.fromFormatted("🔢")),
            )
            .addFiles(FileUpload.fromData(getResource("assets/games/coinflip.png"), "coinflip.png"))
            .queue()
    }
}