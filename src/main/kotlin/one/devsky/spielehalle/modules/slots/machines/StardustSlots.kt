package one.devsky.spielehalle.modules.slots.machines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import one.devsky.spielehalle.Spielehalle
import one.devsky.spielehalle.extensions.asCodeBlock
import one.devsky.spielehalle.extensions.times
import one.devsky.spielehalle.extensions.zeroArray
import one.devsky.spielehalle.modules.slots.machines.interfaces.Reel
import one.devsky.spielehalle.modules.slots.machines.interfaces.ReelSymbol
import one.devsky.spielehalle.modules.slots.machines.interfaces.SlotMachine
import one.devsky.spielehalle.modules.slots.utils.buildGame
import one.devsky.spielehalle.utils.Emojis
import one.devsky.spielehalle.utils.Environment
import one.devsky.spielehalle.utils.TempStorage
import kotlin.time.Duration.Companion.seconds

class StardustSlots : SlotMachine {
    override val identifier: String = "stardust_slots"

    private val reel = Reel(
        ReelSymbol("1", 1, Emojis.SLOT1),
        ReelSymbol("2", 2, Emojis.SLOT2),
        ReelSymbol("3", 3, Emojis.SLOT3),
        ReelSymbol("4", 4, Emojis.SLOT4),
        ReelSymbol("5", 5, Emojis.SLOT5),
        ReelSymbol("6", 6, Emojis.SLOT6),
        ReelSymbol("7", 7, Emojis.SLOT7),
    )

    override val name: String
        get() = "Stardust Slots"

    override val description: String
        get() = "Eine SlotMachine angeleht an die berühmte Starburst SlotMachine"

    override val maxBet: Int
        get() = 100

    override val minBet: Int
        get() = 10

    override val reels: List<Reel>
        get() = listOf(
            reel,
            reel,
            reel,
            reel,
            reel
        )

    override val rows: Int
        get() = 3


    override var isRunning: Boolean = false
    override var player: User? = null
    override var currentReelPositions: Array<Int> = zeroArray(reels.size)
    override var message: Message? = null

    private fun initMessage(): Boolean {
        val channel = Environment.getEnv("CHANNEL_SLOTS")?.let { Spielehalle.instance.jda.getTextChannelById(it) } ?: return false

        val threadId = TempStorage.readTempFileAsString("slots.${identifier}.thread")
        var thread = if (threadId.isNotEmpty()) channel.threadChannels.find { it.id == threadId } else null
        if (thread == null) thread = channel.createThreadChannel(name).complete()

        val messageId = TempStorage.readTempFileAsString("slots.${identifier}.message")
        var message = if (messageId.isNotEmpty()) thread?.retrieveMessageById(messageId)?.complete() else null
        if (message == null) message = thread?.sendMessage("Loading...")?.complete()

        this.message = message
        updateMessage()

        thread?.id?.let { TempStorage.saveTempFile("slots.${identifier}.thread", it) }
        message?.id?.let { TempStorage.saveTempFile("slots.${identifier}.message", it) }
        return true
    }

    override fun run() {
        if (message == null && !initMessage()) {
            player = null
            error("Could not initialize message")
        }

        isRunning = true
        currentReelPositions = zeroArray(reels.size)
        updateMessage()

        CoroutineScope(Dispatchers.Default).launch {
            delay(5.seconds)
            for (i in reels.indices) {
                currentReelPositions[i] = (0 until reels[i].symbols.size).random()
                updateMessage(i + 1)
                delay(3.seconds)
            }
            isRunning = false
            player = null
            updateMessage()
        }
    }

    override fun updateMessage(showing: Int) {
        val embed = EmbedBuilder()
            .setColor(0x4bcffa)
            .setTitle("${3.times(Emojis.EMBED_BACKGROUND.formatted)}SlotMachine - $name")
            .setDescription(description)
            .addField("Mindesteinsatz", "$${minBet}".asCodeBlock(), true)
            .addField("Höchsteinsatz", "$${maxBet}".asCodeBlock(), true)
            .build()

        val embed2 = EmbedBuilder()
            .setColor(0x4bcffa)
            .addField(Emojis.EMBED_BACKGROUND.formatted, buildGame(isRunning, showing), true)
            .addField(Emojis.EMBED_BACKGROUND.formatted, Emojis.EMBED_BACKGROUND.formatted, true)
            .setFooter("Spielehalle | Slots", message?.jda?.selfUser?.avatarUrl)
            .build()

        message?.editMessageEmbeds(embed, embed2)
            ?.setContent("")
            ?.setActionRow(
                Button.secondary("slots.${identifier}.play", "Spielen")
                    .withEmoji(Emojis.SLOTMACHINE),
                Button.secondary("slots.${identifier}.info", "Info")
                    .withEmoji(Emojis.TERMINAL),
            )?.queue()
    }
}