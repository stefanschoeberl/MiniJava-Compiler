package dev.ssch.minijava.ast

data class Module (
    val types: MutableList<FuncType> = mutableListOf(),
    val funcs: MutableList<Function> = mutableListOf(),
    val imports: MutableList<Import> = mutableListOf(),
    val exports: MutableList<Export> = mutableListOf()
) {
    fun declareType(type: FuncType): Int {
        val index = types.indexOf(type)
        if (index == -1) {
            types.add(type)
            return types.size - 1
        } else {
            return index
        }
    }

    fun declareFunction(function: Function): Int {
        funcs.add(function)
        return imports.size + funcs.size - 1
    }

    fun importFunction(import: Import): Int {
        // TODO: check, if no module function has been defined yet

        imports.add(import)
        return imports.size - 1
    }
}