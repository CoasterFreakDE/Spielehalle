package one.devsky.spielehalle.modules.setup

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import one.devsky.spielehalle.annotations.PermissionScope
import one.devsky.spielehalle.annotations.SlashCommand
import one.devsky.spielehalle.interfaces.HasSubcommands

@SlashCommand("setup", "Erzeuge Panels für die verschiedenen Systeme.", permissionScope = PermissionScope.ADMIN)
class SetupCommandListener : ListenerAdapter(), HasSubcommands {

    private val panels = mapOf(
        "stats" to StatsSetup::class.java,
        "coinflip" to CoinflipSetup::class.java,
        "slots" to SlotSetup::class.java,
    )

    /**
     * Returns a list of subcommands.
     *
     * @return The list of subcommands as a List of [SubcommandData] objects.
     */
    override fun getSubCommands(): List<SubcommandData> {
        return panels.map {
            SubcommandData(it.key, "Erzeuge das Panel für ${it.key}")
        }
    }

    /**
     * Handles the interaction with a slash command.
     *
     * @param event The `SlashCommandInteractionEvent` representing the interaction event.
     * @return Unit
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if(name != "setup") return@with

        if (panels.containsKey(subcommandName)) {
            val panel = panels[subcommandName]?.getConstructor(TextChannel::class.java)?.newInstance(channel)
            panel?.createPanel()
            reply("Das Panel wurde erfolgreich erstellt!").setEphemeral(true).queue()
            return@with
        }
        reply("Dieses Panel existiert noch nicht!").setEphemeral(true).queue()
    }
}