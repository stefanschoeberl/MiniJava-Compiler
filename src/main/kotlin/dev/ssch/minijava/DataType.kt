package dev.ssch.minijava

sealed class DataType {
    sealed class PrimitiveType(val name: String, val sizeInBytes: Int): DataType() {
        companion object {
            fun fromString(s: String): PrimitiveType? {
                return when (s) {
                    "int" -> PrimitiveType.Integer
                    "boolean" -> PrimitiveType.Boolean
                    "float" -> PrimitiveType.Float
                    else -> null
                }
            }
        }

        object Integer: PrimitiveType("int", 4)
        object Boolean: PrimitiveType("boolean", 1)
        object Float: PrimitiveType("float", 4)

        override fun toString(): String {
            return name
        }

        override fun sizeInBytes(): Int {
            return sizeInBytes
        }
    }

    data class Array(val elementType: DataType): DataType() {
        override fun toString(): String {
            return "$elementType[]"
        }

        override fun sizeInBytes(): Int {
            return 4
        }
    }

    abstract fun sizeInBytes(): Int
}