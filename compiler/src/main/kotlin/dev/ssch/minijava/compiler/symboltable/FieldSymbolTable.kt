package dev.ssch.minijava.compiler.symboltable

import dev.ssch.minijava.compiler.DataType

class FieldSymbolTable {

    data class FieldInfo (
        val type: DataType,
        var getterAddress: Int,
        var setterAddress: Int
    )

    val fields = mutableMapOf<String, FieldInfo>()

    fun isDeclared(name: String): Boolean {
        return fields.containsKey(name)
    }

    fun declareField(getterAddress: Int, setterAddress: Int, name: String, type: DataType) {
        fields[name] = FieldInfo(type, getterAddress, setterAddress)
    }

    fun findFieldInfo(name: String): FieldInfo? {
        return fields[name]
    }
}