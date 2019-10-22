package dev.ssch.minijava

class SymbolTable {

    class SymbolInformation (
        val address: Int,
        val type: DataType
    )

    private val symbols: MutableMap<String, SymbolInformation> = mutableMapOf()

    fun declareVariable(name: String, type: DataType): Int {
        val addr = symbols.size
        symbols[name] = SymbolInformation(addr, type)
        return addr
    }

    fun isDeclared(name: String): Boolean = symbols.containsKey(name)

    fun addressOf(name: String): Int = symbols[name]!!.address
    fun typeOf(name: String): DataType = symbols[name]!!.type
}