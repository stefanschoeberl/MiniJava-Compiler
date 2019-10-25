package dev.ssch.minijava


enum class DataType {
    Integer, Boolean;

    companion object {
        fun fromString(s: String): DataType? {
            return when (s) {
                "int" -> DataType.Integer
                "boolean" -> DataType.Boolean
                else -> null
            }
        }
    }
}