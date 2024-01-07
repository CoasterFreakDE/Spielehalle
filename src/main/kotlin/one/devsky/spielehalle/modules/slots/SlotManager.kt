package one.devsky.spielehalle.modules.slots

import one.devsky.spielehalle.modules.slots.machines.StardustSlots
import one.devsky.spielehalle.modules.slots.machines.interfaces.SlotMachine

object SlotManager {

    private val slotMachines: Map<String, SlotMachine> = mapOf(
        "stardust_slots" to StardustSlots()
    )

    fun getSlotMachine(identifier: String): SlotMachine? {
        return slotMachines[identifier]
    }

    fun getSlotMachines(): List<SlotMachine> {
        return slotMachines.values.toList()
    }
}