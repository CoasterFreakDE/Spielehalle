package one.devsky.spielehalle.modules.slots.machines.interfaces

import net.dv8tion.jda.api.entities.User

interface SinglePlayerGame: RunnableGame {

    var player: User?

}