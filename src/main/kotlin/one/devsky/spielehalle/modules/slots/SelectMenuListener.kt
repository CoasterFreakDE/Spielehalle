package one.devsky.spielehalle.modules.slots

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import one.devsky.spielehalle.db.cache.casino.CasinoCache
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.db.model.enums.Game
import one.devsky.spielehalle.modules.slots.machines.interfaces.SlotMachine

class SelectMenuListener : ListenerAdapter() {

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) = with(event) {
        if (componentId.startsWith("slots.")) {
            val identifier = componentId.split(".")[1]
            val slotMachine = SlotManager.getSlotMachine(identifier) ?: return

            if (componentId.endsWith(".bet")) {
                handleBetSelect(event, slotMachine)
            }

            if (componentId.endsWith(".spin")) {
                val bet = componentId.split(".")[2].toInt()
                handleSpinSelect(event, slotMachine, bet)
            }
        }
    }

    private fun handleBetSelect(event: StringSelectInteractionEvent, slotMachine: SlotMachine) {
        val bet = event.selectedOptions.first().value.toInt()

        event.reply("Wie oft möchtest du drehen?").setEphemeral(true).setComponents(
            ActionRow.of(
                StringSelectMenu.create("slots.${slotMachine.identifier}.$bet.spin")
                    .addOptions(
                        SelectOption.of("Ein mal", "1"),
                        SelectOption.of("Drei mal", "3"),
                        SelectOption.of("Fünf mal", "5"),
                        SelectOption.of("Zehn mal", "10"),
                        SelectOption.of("Zwanzig mal", "20"),
                        SelectOption.of("Fünfzig mal", "50"),
                    )
                    .setPlaceholder("Spins")
                    .setMaxValues(1)
                    .build()
            )).queue()



    }

    private fun handleSpinSelect(event: StringSelectInteractionEvent, slotMachine: SlotMachine, bet: Int) {
        if (slotMachine.isRunning || slotMachine.player != null) {
            event.reply("Bitte warte einen Moment, bis ${slotMachine.player?.asMention} fertig ist.").setEphemeral(true).queue()
            return
        }

        val spins = event.selectedOptions.first().value.toInt()

        val casinoUser = CasinoUserCache.getUser(event.user.id)
        if (casinoUser.money < bet) {
            event.reply("Du hast nicht genug Geld!").setEphemeral(true).queue()
            return
        }
        CasinoUserCache.saveUser(casinoUser.copy(money = casinoUser.money - bet))
        CasinoCache.modifyMoney(bet.toDouble(), Game.SLOTS, event.user)

        slotMachine.autoRolls = spins
        slotMachine.einsatz = bet
        slotMachine.player = event.user
        slotMachine.run()

        event.reply("Das Spiel wurde gestartet!").setEphemeral(true).queue()
    }
}