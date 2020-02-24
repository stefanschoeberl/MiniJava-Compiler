package dev.ssch.minijava

enum class DataType {
    Integer, Boolean, Float;

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
        }
    }
}