package dev.ssch.minijava

class FieldSymbolTable {

    data class FieldInfo (
        val offset: Int,
        val type: DataType
    )

    private val fields = mutableMapOf<String, FieldInfo>()
    private var currentSize = 0

    fun isDeclared(name: String): Boolean {
        return fields.containsKey(name)
    }

    fun declareField(name: String, type: DataType) {
        fields[name] = FieldInfo(currentSize, type)
        currentSize += type.sizeInBytes()
    }

    fun findFieldInfo(name: String): FieldInfo? {
        return fields[name]
    }

    fun getSize(): Int {
        return currentSize
    }
}