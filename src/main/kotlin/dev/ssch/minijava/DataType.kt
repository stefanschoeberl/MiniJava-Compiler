package dev.ssch.minijava

sealed class DataType {
    object Integer: DataType()
    object Boolean: DataType()
    object Float: DataType()
    data class Array(val elementType: DataType): DataType()

    companion object {
        fun fromString(s: String): DataType? {
            return when (s) {
                "int" -> Integer
                "boolean" -> Boolean
                "float" -> Float
                else -> null
            }
        }
    }

    override fun toString(): String {
        return when(this) {
            Integer -> "int"
            Boolean -> "boolean"
            Float -> "float"
            is Array -> "$elementType[]"
        }
    }

    fun sizeInBytes(): Int {
        return when (this) {
            Integer -> 4
            Boolean -> 1
            Float -> 4
            is Array -> 4
        }
    }
}