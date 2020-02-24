package dev.ssch.minijava

import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.ast.ValueType
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
        DataType.Integer -> ValueType.I32
        DataType.Boolean -> ValueType.I32
        DataType.Float -> ValueType.F32
    }
}

val dataTypeWideningConversions = hashMapOf(
    Pair(Pair(DataType.Integer, DataType.Float), listOf(Instruction.f32_convert_i32_s()))
)

fun DataType.assignTypeTo(other: DataType): List<Instruction>? {
    if (this == other) {
        return listOf()
    } else {
        return dataTypeWideningConversions[Pair(this, other)]
    }
}

fun MethodSymbolTable.MethodSignature.externalName(): String {
    return this.name + this.parameterTypes.map { "#$it" }.joinToString()
}