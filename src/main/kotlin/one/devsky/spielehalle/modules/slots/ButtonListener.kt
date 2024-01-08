package one.devsky.spielehalle.modules.slots

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import one.devsky.spielehalle.modules.slots.machines.interfaces.SlotMachine

class ButtonListener : ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) = with(event) {
        if (componentId.startsWith("slots.") && componentId.endsWith(".play")) {
            val identifier = componentId.split(".")[1]
            val slotMachine = SlotManager.getSlotMachine(identifier) ?: return
            handleStartButton(event, slotMachine)
            return@with
        }

        if (componentId.startsWith("slots.") && componentId.endsWith(".stop")) {
            val identifier = componentId.split(".")[1]
            val slotMachine = SlotManager.getSlotMachine(identifier) ?: return
            handleStopButton(event, slotMachine)
            return@with
        }
    }

    private fun handleStartButton(event: ButtonInteractionEvent, slotMachine: SlotMachine) {
        if (slotMachine.isRunning || slotMachine.player != null) {
            event.reply("Bitte warte einen Moment, bis ${slotMachine.player?.asMention} fertig ist.").setEphemeral(true).queue()
            return
        }

        event.reply("Wie viel möchtest du setzen?").setEphemeral(true).setComponents(
            ActionRow.of(
                StringSelectMenu.create("slots.${slotMachine.identifier}.bet")
                    .addOptions(
                        SelectOption.of("$10", "10"),
                        SelectOption.of("$15", "15"),
                        SelectOption.of("$20", "20"),
                        SelectOption.of("$25", "25"),
                        SelectOption.of("$50", "50"),
                        SelectOption.of("$75", "75"),
                        SelectOption.of("$100", "100"),
                    )
                    .setPlaceholder("Einsatz")
                    .setMaxValues(1)
                    .build()
            )).queue()
    }

    private fun handleStopButton(event: ButtonInteractionEvent, slotMachine: SlotMachine) {
        if (slotMachine.isRunning && slotMachine.player != event.user) {
            event.reply("Du kannst nicht das Spiel von anderen Spielern unterbrechen.").setEphemeral(true).queue()
            return
        }

        if (!slotMachine.isRunning) {
            event.reply("Es läuft aktuell kein Spiel.").setEphemeral(true).queue()
            return
        }

        slotMachine.autoRolls = 1
        event.reply("${slotMachine.name} wird nach dieser Runde gestoppt.").setEphemeral(true).queue()
    }
}