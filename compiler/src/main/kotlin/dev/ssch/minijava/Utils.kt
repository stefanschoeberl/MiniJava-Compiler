package dev.ssch.minijava

import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.ClassSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable
import dev.ssch.minijava.wasm.ast.Instruction
import dev.ssch.minijava.wasm.ast.ValueType

fun <T> MutableList<T>.removeFirstOrNull(): T? {
    return if (this.isEmpty()) {
        null
    } else {
        this.removeAt(0)
    }
}

fun DataType.toWebAssemblyType(): ValueType {
    return when (this) {
        DataType.PrimitiveType.Integer -> ValueType.I32
        DataType.PrimitiveType.Boolean -> ValueType.I32
        DataType.PrimitiveType.Float -> ValueType.F32
        DataType.PrimitiveType.Char -> ValueType.I32
        is DataType.Array -> ValueType.I32
        is DataType.ReferenceType -> ValueType.I32
        DataType.NullType -> TODO()
    }
}

fun MiniJavaParser.TypeDefinitionContext.getDataType(classSymbolTable: ClassSymbolTable): DataType? {
    fun asReferenceType(name: String): DataType? {
        return if (classSymbolTable.classes.containsKey(name)) {
            DataType.ReferenceType(name)
        } else {
            null
        }
    }

    return when (val ctx = this) {
        is MiniJavaParser.SimpleTypeContext -> {
            val primitiveType = DataType.PrimitiveType.fromString(ctx.IDENT().text)
            primitiveType ?: asReferenceType(ctx.IDENT().text)
        }
        is MiniJavaParser.ArrayTypeContext -> {
            val primitiveType = DataType.PrimitiveType.fromString(ctx.IDENT().text)
            if (primitiveType != null) {
                DataType.Array(primitiveType)
            } else {
                asReferenceType(ctx.IDENT().text)?.let { DataType.Array(it) }
            }
        }
        else -> null
    }
}

val dataTypeWideningConversions = hashMapOf(
    Pair(Pair(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float), listOf(Instruction.f32_convert_i32_s)),
    Pair(Pair(DataType.PrimitiveType.Char, DataType.PrimitiveType.Integer), listOf()),
    Pair(Pair(DataType.PrimitiveType.Char, DataType.PrimitiveType.Float), listOf(Instruction.f32_convert_i32_s))
)

val dataTypeCastingConversions = hashMapOf(
    Pair(Pair(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float), listOf(Instruction.f32_convert_i32_s)),
    Pair(Pair(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Char), listOf()),
    Pair(Pair(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer), listOf(Instruction.i32_trunc_f32_s)),
    Pair(Pair(DataType.PrimitiveType.Float, DataType.PrimitiveType.Char), listOf(Instruction.i32_trunc_f32_s)),
    Pair(Pair(DataType.PrimitiveType.Char, DataType.PrimitiveType.Integer), listOf()),
    Pair(Pair(DataType.PrimitiveType.Char, DataType.PrimitiveType.Float), listOf(Instruction.f32_convert_i32_s))
)

fun DataType.assignTypeTo(other: DataType): List<Instruction>? {
    return if (this == other) {
        listOf()
    } else if (this == DataType.NullType && (other is DataType.ReferenceType || other is DataType.Array)) {
        listOf()
    } else {
        dataTypeWideningConversions[Pair(this, other)]
    }
}

fun DataType.castTypeTo(other: DataType): List<Instruction>? {
    return if (this == other) {
        listOf()
    } else {
        dataTypeCastingConversions[Pair(this, other)]
    }
}

fun MethodSymbolTable.MethodSignature.externalName(): String {
    return this.name + this.parameterTypes.joinToString("") { "#$it" }
}

fun externalConstructorName(className: String): String {
    return "new_$className"
}

fun externalGetterName(className: String, fieldName: String): String {
    return "get_$className.$fieldName"
}

fun externalSetterName(className: String, fieldName: String): String {
    return "set_$className.$fieldName"
}