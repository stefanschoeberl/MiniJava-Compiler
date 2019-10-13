package dev.ssch.minijava

import java.io.File

class WebAssemblyAssembler {

    fun assemble(wat: File, wasm: File) {
        listOf("wat2wasm", wat.absolutePath, "-o", wasm.absolutePath).runCommand(File(System.getProperty("user.dir")))
    }
}