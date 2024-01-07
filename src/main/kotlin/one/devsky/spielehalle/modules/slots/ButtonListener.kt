package one.devsky.spielehalle.modules.slots

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
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

        slotMachine.player = event.user
        slotMachine.run()

        event.reply("Das Spiel wurde gestartet!").setEphemeral(true).queue()
    }
}