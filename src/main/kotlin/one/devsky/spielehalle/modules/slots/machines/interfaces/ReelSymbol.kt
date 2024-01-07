package one.devsky.spielehalle.modules.slots.machines.interfaces

import net.dv8tion.jda.api.entities.emoji.Emoji

data class ReelSymbol(
    val name: String,
    val value: Int,
    val emoji: Emoji
)