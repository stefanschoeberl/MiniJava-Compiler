package dev.ssch.minijava.wasm

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