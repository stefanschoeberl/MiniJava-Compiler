package dev.ssch.minijava.wasm

import java.io.File

class WebAssemblyRunner {

    fun run(file: String): String {
        return listOf("node", "run.js", file).runCommand(File(System.getProperty("user.dir")).parentFile)
    }
}