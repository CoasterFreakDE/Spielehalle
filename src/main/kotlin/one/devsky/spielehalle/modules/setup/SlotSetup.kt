package one.devsky.spielehalle.modules.setup

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button
import one.devsky.spielehalle.extensions.asCodeBlock
import one.devsky.spielehalle.extensions.times
import one.devsky.spielehalle.modules.slots.SlotManager
import one.devsky.spielehalle.modules.slots.machines.StardustSlots
import one.devsky.spielehalle.modules.slots.machines.interfaces.SlotMachine
import one.devsky.spielehalle.modules.slots.utils.buildGame
import one.devsky.spielehalle.utils.Emojis
import one.devsky.spielehalle.utils.TempStorage

class SlotSetup(private val channel: TextChannel): PanelCreation {

    override fun createPanel() {

        for (slotMachine in SlotManager.getSlotMachines()) {
            createSlotMachinePanel(slotMachine)
        }
    }

    private fun createSlotMachinePanel(slotMachine: SlotMachine) {
        val threadId = TempStorage.readTempFileAsString("slots.${slotMachine.identifier}.thread")
        var thread = if (threadId.isNotEmpty()) channel.threadChannels.find { it.id == threadId } else null
        if (thread == null) thread = channel.createThreadChannel(slotMachine.name).complete()

        val messageId = TempStorage.readTempFileAsString("slots.${slotMachine.identifier}.message")
        var message = if (messageId.isNotEmpty()) thread?.retrieveMessageById(messageId)?.complete() else null
        if (message == null) message = thread?.sendMessage("Loading...")?.complete()

        slotMachine.message = message
        slotMachine.updateMessage()

        thread?.id?.let { TempStorage.saveTempFile("slots.${slotMachine.identifier}.thread", it) }
        message?.id?.let { TempStorage.saveTempFile("slots.${slotMachine.identifier}.message", it) }

        slotMachine.run()
    }
}