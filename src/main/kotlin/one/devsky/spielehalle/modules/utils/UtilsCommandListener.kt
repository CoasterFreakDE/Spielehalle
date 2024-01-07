package one.devsky.spielehalle.modules.utils

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import one.devsky.spielehalle.annotations.PermissionScope
import one.devsky.spielehalle.annotations.SlashCommand
import one.devsky.spielehalle.interfaces.HasSubcommands

@SlashCommand("utils", "Nützliche Befehle für den Server.", permissionScope = PermissionScope.ADMIN)
class UtilsCommandListener : ListenerAdapter(), HasSubcommands {
    override fun getSubCommands(): List<SubcommandData> {
        return listOf(
            SubcommandData("emojis", "Listet alle Emojis auf.")
        )
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if(name != "utils") return@with

        when(subcommandName) {
            "emojis" -> {
                if (event.guild == null) {
                    event.reply("Dieser Befehl kann nur auf einem Server ausgeführt werden!").setEphemeral(true).queue()
                    return@with
                }

                val emojis = event.guild!!.emojis.sortedBy { it.name }.joinToString("\n") { "${it.asMention} - ${it.name} - `${it.asMention}`" } + "\n"

                // Splitte die Nachricht in mehrere Nachrichten, wenn sie länger als 2000 Zeichen ist (an dem letzten Linebreak)
                val splitEmojis = emojis.chunked(2000) { it.substringBeforeLast("\n") }

                splitEmojis.forEach {
                    hook.sendMessage(it.toString()).setEphemeral(true).queue()
                }

                reply("Alle Emojis wurden gesendet!").setEphemeral(true).queue()
            }
        }
    }
}

private fun CharSequence.substringBeforeLast(seperator: String): CharSequence {
    val index = lastIndexOf(seperator)
    return if (index == -1) {
        this
    } else {
        substring(0, index)
    }
}