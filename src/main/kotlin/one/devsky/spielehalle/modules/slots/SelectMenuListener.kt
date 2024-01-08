package one.devsky.spielehalle.modules.slots

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import one.devsky.spielehalle.db.cache.casino.CasinoCache
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.db.model.enums.Game
import one.devsky.spielehalle.extensions.isVIP
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
        event.deferEdit().queue()

        if (event.member == null) return
        if (event.member?.isVIP() == false) {
            handleStart(event, slotMachine, bet, 1, false)
            return
        }

        event.hook.editOriginal("Wie oft möchtest du drehen?").setComponents(
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
        event.deferEdit().queue()

        val spins = event.selectedOptions.first().value.toInt()
        handleStart(event, slotMachine, bet, spins, true)
    }

    private fun handleStart(event: StringSelectInteractionEvent, slotMachine: SlotMachine, bet: Int, spins: Int = 1, isVip: Boolean) {
        if (slotMachine.isRunning || slotMachine.player != null) {
            event.hook.editOriginal("Bitte warte einen Moment, bis ${slotMachine.player?.asMention} fertig ist.").setComponents().queue()
            return
        }

        val casinoUser = CasinoUserCache.getUser(event.user.id)
        if (casinoUser.money < bet) {
            event.hook.editOriginal("Du hast nicht genug Geld!").setComponents().queue()
            return
        }
        casinoUser.addEXP(1)
        CasinoUserCache.saveUser(casinoUser.copy(
            money = casinoUser.money - bet,
            gamesPlayed = casinoUser.gamesPlayed + 1
        ))
        CasinoCache.modifyMoney(bet.toDouble(), Game.SLOTS, event.user)

        slotMachine.autoRolls = spins
        slotMachine.einsatz = bet
        slotMachine.player = event.user
        slotMachine.run()

        event.hook.editOriginal(
            """
                Du hast ${slotMachine.name} mit einem Einsatz von $$bet gestartet.
                ${if (spins > 1) "Es wird $spins mal gedreht." else ""}
                ${if (!isVip) "Mit VIP kannst du mehrere Spins auf einmal starten." else ""}
            """.trimIndent()
        ).setComponents().queue()
    }
}