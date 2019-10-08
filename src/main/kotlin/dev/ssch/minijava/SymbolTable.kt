package dev.ssch.minijava

class SymbolTable {

    private val symbols: MutableMap<String, Int> = mutableMapOf()

    fun declareVariable(name: String): Int {
        val addr = symbols.size
        symbols[name] = addr
        return addr
    }

    fun isDeclared(name: String): Boolean = symbols.containsKey(name)


    fun addressOf(name: String): Int = symbols[name]!!
}