package dev.ssch.minijava

import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.ast.ValueType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.ClassSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable
import java.io.File
import java.util.concurrent.TimeUnit

// https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
fun List<String>.runCommand(workingDir: File): String {
    val process = ProcessBuilder(this)
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    process.waitFor(60, TimeUnit.MINUTES)

    val error = process.errorStream.bufferedReader().readText()
    if (error.isNotEmpty()) {
        throw RuntimeException("WebAssembly Execution Error:\n$error")
    }

    return process.inputStream.bufferedReader().readText()
}

fun <T> MutableList<T>.removeFirstOrNull(): T? {
    if (this.isEmpty()) {
        return null
    } else {
        return this.removeAt(0)
    }
}

fun DataType.toWebAssemblyType(): ValueType {
    return when (this) {
        DataType.PrimitiveType.Integer -> ValueType.I32
        DataType.PrimitiveType.Boolean -> ValueType.I32
        DataType.PrimitiveType.Float -> ValueType.F32
        is DataType.Array -> ValueType.I32
        is DataType.ReferenceType -> ValueType.I32
    }
}

fun DataType.getStoreMemoryInstruction(): Instruction {
    return when (this) {
        DataType.PrimitiveType.Integer -> Instruction.i32_store
        DataType.PrimitiveType.Boolean -> Instruction.i32_store8
        DataType.PrimitiveType.Float -> Instruction.f32_store
        is DataType.Array -> Instruction.i32_store
        is DataType.ReferenceType -> Instruction.i32_store
    }
}

fun DataType.getLoadMemoryInstruction(): Instruction {
    return when (this) {
        DataType.PrimitiveType.Integer -> Instruction.i32_load
        DataType.PrimitiveType.Boolean -> Instruction.i32_load8_s
        DataType.PrimitiveType.Float -> Instruction.f32_load
        is DataType.Array -> Instruction.i32_load
        is DataType.ReferenceType -> Instruction.i32_load
    }
}

fun MiniJavaParser.TypeDefinitionContext.getDataType(classSymbolTable: ClassSymbolTable): DataType? {
    return when (val ctx = this) {
        is MiniJavaParser.SimpleTypeContext -> {
            val primitiveType = DataType.PrimitiveType.fromString(ctx.IDENT().text)
            if (primitiveType != null) {
                primitiveType
            } else if (classSymbolTable.classes.containsKey(ctx.IDENT().text)) {
                DataType.ReferenceType(ctx.IDENT().text)
            } else {
                null
            }
        }
        is MiniJavaParser.ArrayTypeContext -> DataType.PrimitiveType.fromString(ctx.IDENT().text)?.let { DataType.Array(it) }
        else -> null
    }
}

val dataTypeWideningConversions = hashMapOf(
    Pair(Pair(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float), listOf(Instruction.f32_convert_i32_s))
)

val dataTypeCastingConversions = hashMapOf(
    Pair(Pair(DataType.PrimitiveType.Integer, DataType.PrimitiveType.Float), listOf(Instruction.f32_convert_i32_s)),
    Pair(Pair(DataType.PrimitiveType.Float, DataType.PrimitiveType.Integer), listOf(Instruction.i32_trunc_f32_s))
)

fun DataType.assignTypeTo(other: DataType): List<Instruction>? {
    if (this == other) {
        return listOf()
    } else {
        return dataTypeWideningConversions[Pair(this, other)]
    }
}

fun DataType.castTypeTo(other: DataType): List<Instruction>? {
    if (this == other) {
        return listOf()
    } else {
        return dataTypeCastingConversions[Pair(this, other)]
    }
}

fun MethodSymbolTable.MethodSignature.externalName(): String {
    return this.name + this.parameterTypes.map { "#$it" }.joinToString()
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