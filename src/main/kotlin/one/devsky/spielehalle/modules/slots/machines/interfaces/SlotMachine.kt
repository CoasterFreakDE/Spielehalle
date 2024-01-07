package one.devsky.spielehalle.modules.slots.machines.interfaces

import net.dv8tion.jda.api.entities.Message

interface SlotMachine: SinglePlayerGame {

    val identifier: String
    val name: String
    val description: String

    val maxBet: Int
    val minBet: Int

    val reels: List<Reel>
    val rows: Int

    var currentReelPositions : Array<Int>


    var message: Message?

    fun updateMessage(showing: Int = 0): Unit
}

