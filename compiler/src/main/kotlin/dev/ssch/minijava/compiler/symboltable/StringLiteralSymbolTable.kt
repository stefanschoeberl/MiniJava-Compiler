package dev.ssch.minijava.compiler.symboltable

class StringLiteralSymbolTable {

    private val strings = mutableMapOf<String, Int>()

    fun addressOfString(string: String): Int {
        return strings.computeIfAbsent(string) {
            strings.size + 1 // skip 0 for "null pointer"
        }
    }

    val allStringsByAddress
        get() = strings.entries.map { Pair(it.value, it.key) }
}