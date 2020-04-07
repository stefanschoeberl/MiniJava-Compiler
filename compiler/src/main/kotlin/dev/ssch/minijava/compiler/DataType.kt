package dev.ssch.minijava.compiler

sealed class DataType {
    sealed class PrimitiveType(val name: String): DataType() {
        companion object {
            fun fromString(s: String): PrimitiveType? {
                return when (s) {
                    "int" -> Integer
                    "boolean" -> Boolean
                    "float" -> Float
                    "char" -> Char
                    else -> null
                }
            }
        }

        object Integer: PrimitiveType("int")
        object Boolean: PrimitiveType("boolean")
        object Float: PrimitiveType("float")
        object Char: PrimitiveType("char")

        override fun toString(): String {
            return name
        }
    }

    open class ReferenceType(val name: String): DataType() {
        object StringType: ReferenceType("String")
        object ObjectType: ReferenceType("Object")

        override fun toString(): String {
            return name
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ReferenceType) return false

            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
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