package dev.ssch.minijava

import java.io.File
import java.util.concurrent.TimeUnit

// https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
fun List<String>.runCommand(workingDir: File): String {
    val process = ProcessBuilder(this)
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    process.waitFor(60, TimeUnit.MINUTES)
    return process.inputStream.bufferedReader().readText()
}