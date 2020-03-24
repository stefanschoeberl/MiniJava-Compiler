package dev.ssch.minijava

import dev.ssch.minijava.ast.Module
import java.io.File

class BundleGenerator {

    private val moduleGenerator = WebAssemblyModuleGenerator()
    private val assembler = WebAssemblyAssembler()

    fun generateBundle(module: Module, src: List<File>, outputFolder: File) {
        outputFolder.mkdirs()
        val wat = File(outputFolder, "module.wat")
        val watText = moduleGenerator.toSExpr(module)
        wat.writeText(watText)

        val wasm = File(outputFolder, "module.wasm")
        assembler.assemble(wat, wasm)

        val nativeFolder = File(outputFolder, "native")
        nativeFolder.mkdir()

        val scriptFiles = copyScriptFiles(src, nativeFolder)
        generateModuleJS(outputFolder, nativeFolder, scriptFiles)
    }

    private fun generateModuleJS(outputFolder: File, nativeFolder: File, scriptFiles: List<File>) {
        val scripts = scriptFiles.joinToString { "require('./${nativeFolder.name}/${it.name}')" }
        val content = "module.exports = [$scripts];"
        val moduleJS = File(outputFolder, "module.js")
        moduleJS.writeText(content)
    }

    private fun copyScriptFiles(src: List<File>, nativeFolder: File): List<File> {
        val scriptFiles = src.map {file ->
            val name = file.nameWithoutExtension
            File(file.parent, "$name.js")
        }.filter { it.isFile }

        val copyFromTo = scriptFiles.mapIndexed { index, scriptFile ->
            Pair(scriptFile, File(nativeFolder, "$index.js"))
        }
        copyFromTo.forEach {
            it.first.copyTo(it.second, true)
        }

        return copyFromTo.map { it.second }
    }
}