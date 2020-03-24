package dev.ssch.minijava.symboltable

import dev.ssch.minijava.DataType

class FieldSymbolTable {

    data class FieldInfo (
        val offset: Int,
        val type: DataType,
        var getterAddress: Int,
        var setterAddress: Int
    )

    val fields = mutableMapOf<String, FieldInfo>()
    private var currentSize = 0

    fun isDeclared(name: String): Boolean {
        return fields.containsKey(name)
    }

    fun declareField(getterAddress: Int, setterAddress: Int, name: String, type: DataType) {
        fields[name] = FieldInfo(currentSize, type, getterAddress, setterAddress)
        currentSize += type.sizeInBytes()
    }

    fun findFieldInfo(name: String): FieldInfo? {
        return fields[name]
    }

    fun getSize(): Int {
        return currentSize
    }
}