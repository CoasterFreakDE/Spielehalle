package one.devsky.spielehalle.modules.slots.utils

import one.devsky.spielehalle.extensions.times
import one.devsky.spielehalle.modules.slots.machines.interfaces.SlotMachine
import one.devsky.spielehalle.utils.Emojis
import kotlin.math.max
import kotlin.math.min

fun SlotMachine.buildGame(running: Boolean = false, showing: Int = 0): String {
    val intent = 3.times(Emojis.EMBED_BACKGROUND.formatted)
    val builder = StringBuilder()

    if(isRunning && player != null) {
        builder.append("Es spielt: ${player!!.asMention}\n")
    }

    val boxes = (reels.size + 2).times("⬛ ")

    builder.append("$intent$boxes\n")
    if (rows % 2 == 0) error("Rows must be odd")

    val middle = rows / 2

    rows.times {
        builder.append("$intent⬛ ")
        reels.forEachIndexed { index, slot ->
            if (running && showing <= index) {
                builder.append("${Emojis.RUNNING.formatted} ")
            } else {
                var item = (currentReelPositions[index] + middle - it) % slot.symbols.size
                if (item < 0) item += slot.symbols.size
                builder.append("${slot.symbols[item].emoji.formatted} ")
            }
        }
        builder.append("⬛ \n")
    }

    builder.append("$intent$boxes")
    return builder.toString()
}


private fun formatMulti(multiplier: Int): String {
    var formated = multiplier.toString()
    while (formated.length < 4) {
        formated = "⠀$formated"
    }
    return formated
}