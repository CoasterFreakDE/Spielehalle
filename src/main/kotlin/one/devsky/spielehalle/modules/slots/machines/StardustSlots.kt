package one.devsky.spielehalle.modules.slots.machines

import dev.fruxz.ascend.extension.container.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import one.devsky.spielehalle.Spielehalle
import one.devsky.spielehalle.db.cache.casino.CasinoCache
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.db.model.enums.Game
import one.devsky.spielehalle.extensions.*
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
        ReelSymbol("4", 1, Emojis.SLOT4),
        ReelSymbol("5", 2, Emojis.SLOT5),
        ReelSymbol("6", 2, Emojis.SLOT6),
        ReelSymbol("7", 3, Emojis.SLOT7),
        ReelSymbol("Chip", 5, Emojis.CHIP),
        ReelSymbol("Diamond", 10, Emojis.DIAMOND),
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
            reel.shuffle(),
            reel.shuffle(),
            reel.shuffle(),
            reel.shuffle()
        )

    override val rows: Int
        get() = 3


    override var isRunning: Boolean = false
    override var player: User? = null
    override var currentReelPositions: Array<Int> = zeroArray(reels.size)
    override var message: Message? = null
    override var einsatz: Int = minBet
    override var autoRolls: Int = 0
    var lastWin: Int = 0

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
            delay(10.seconds)
            for (i in reels.indices) {
                currentReelPositions[i] = (0 until reels[i].symbols.size).random()
            }
            updateMessage(99)
            delay(10.seconds)
            sendAuswertung()

            if (autoRolls > 1) {
                autoRolls--

                val casinoUser = player?.id?.let { CasinoUserCache.getUser(it) } ?: return@launch
                if (casinoUser.money < einsatz) {
                    message?.channel?.sendMessage("AutoRoll von ${player!!.asMention} wurde abgebrochen. Dein Geld ist leer.")?.queue { msg ->
                        msg.delete().queueAfter(10.seconds)
                    }
                    isRunning = false
                    player = null
                    updateMessage()
                    return@launch
                }
                CasinoUserCache.saveUser(casinoUser.copy(money = casinoUser.money - einsatz.toDouble()))
                CasinoCache.modifyMoney(einsatz.toDouble(), Game.SLOTS, player)

                run()
                return@launch
            }
            autoRolls = 0
            isRunning = false
            player = null
            updateMessage()
        }
    }

    private fun sendAuswertung() {
        val same = currentReelPositions.same
        val wins = when {
            same.containsValue(5) -> {
                val value = same[0] ?: return
                reels[0].symbols[value].value * 10
            }
            same.containsValue(4) -> {
                val value = same.first { it.value == 4 }.key
                reels[0].symbols[value].value * 5
            }
            same.containsValue(3) -> {
                val value = same.first { it.value == 3 }.key
                reels[0].symbols[value].value * 2
            }
            else -> 0
        } * einsatz

        val casinoUser = player?.let { CasinoUserCache.getUser(it.id) } ?: return

        if (wins > 0) {
            lastWin = wins
            CasinoCache.modifyMoney(-wins.toDouble(), Game.SLOTS, player)
            CasinoUserCache.saveUser(casinoUser.copy(money = casinoUser.money + wins, winnings = casinoUser.winnings + wins))

            val embed = EmbedBuilder()
                .setColor(0x4bcffa)
                .setDescription("Auswertung von ${player?.asMention}")
                .addField("Gewinn", "$$wins".asCodeBlock(), true)
                .build()

            message?.channel?.sendMessageEmbeds(embed)?.queue { msg ->
                msg.delete().queueAfter(10.seconds)
            }
            return
        }
        CasinoUserCache.saveUser(casinoUser.copy(losses = casinoUser.losses + einsatz))
    }

    override fun updateMessage(showing: Int) {
        val embed = EmbedBuilder()
            .setColor(0x4bcffa)
            .setTitle("${3.times(Emojis.EMBED_BACKGROUND.formatted)}SlotMachine - $name")
            .setDescription(description)
            .addField("Mindesteinsatz", "$${minBet}".asCodeBlock(), true)
            .addField("Höchsteinsatz", "$${maxBet}".asCodeBlock(), true)
            .addField("Letzter Gewinn", "$${lastWin}".asCodeBlock(), true)
            .also {
                if(isRunning && player != null) {
                    it.addField("Spieler", player!!.asMention, true)
                    it.addField("Aktueller Einsatz", "$${einsatz}".asCodeBlock(), true)
                    it.addField("AutoSpins", "$autoRolls".asCodeBlock(), true)
                }
            }
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
                Button.secondary("slots.${identifier}.stop", "Stop")
                    .withEmoji(Emojis.STOPPEN),
                Button.secondary("slots.${identifier}.info", "Info")
                    .withEmoji(Emojis.TERMINAL),
            )?.queue()
    }
}