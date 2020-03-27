package dev.ssch.minijava

sealed class DataType {
    sealed class PrimitiveType(val name: String): DataType() {
        companion object {
            fun fromString(s: String): PrimitiveType? {
                return when (s) {
                    "int" -> Integer
                    "boolean" -> Boolean
                    "float" -> Float
                    else -> null
                }
            }
        }

        object Integer: PrimitiveType("int")
        object Boolean: PrimitiveType("boolean")
        object Float: PrimitiveType("float")

        override fun toString(): String {
            return name
        }
    }

    data class ReferenceType(val name: String): DataType() {
        override fun toString(): String {
            return name
        }
    }

    data class Array(val elementType: DataType): DataType() {
        override fun toString(): String {
            return "$elementType[]"
        }
    }

    object NullType: DataType() {
        override fun toString(): String {
            return "null"
        }
    }
}