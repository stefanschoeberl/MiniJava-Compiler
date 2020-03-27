package dev.ssch.minijava.ast

data class Module (
    val types: MutableList<FuncType> = mutableListOf(),
    val funcs: MutableList<Function> = mutableListOf(),
    val imports: MutableList<Import> = mutableListOf(),
    val exports: MutableList<Export> = mutableListOf()
) {
    fun declareType(type: FuncType): Int {
        val index = types.indexOf(type)
        return if (index == -1) {
            types.add(type)
            types.size - 1
        } else {
            index
        }
    }

    fun declareFunction(function: Function) {
        funcs.add(function)
    }

    fun importFunction(import: Import): Int {
        imports.add(import)
        return imports.size - 1
    }
}