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
}