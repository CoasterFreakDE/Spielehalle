package one.devsky.spielehalle.modules.slots.machines.interfaces

class Reel(vararg var symbols: ReelSymbol) {

    fun shuffle(): Reel {
        val newSymbols = symbols.toMutableList()
        newSymbols.shuffle()
        return Reel(*newSymbols.toTypedArray())
    }
}
