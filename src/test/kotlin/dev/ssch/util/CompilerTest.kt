package dev.ssch.util

import dev.ssch.minijava.WebAssemblyAssembler
import dev.ssch.minijava.Compiler
import dev.ssch.minijava.WebAssemblyRunner
import dev.ssch.minijava.ModuleGenerator
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class CompilerTest {

    @TempDir
    lateinit var temporaryFolder: File

    fun useStandardLibary(use: Boolean): String {
        return if (use) """
            native void println(int a);
            native void println(boolean a);
        """.trimIndent() else ""
    }

    fun String.runInMainFunction(withStandardLibrary: Boolean = true): String {
        return "public void main() {\n${this.trimIndent().prependIndent("    ")}\n}".compileAndRunMainFunction(withStandardLibrary)
    }

    fun String.compileAndRunMainFunction(withStandardLibrary: Boolean = true): String {
        val compiler = Compiler()
        val source = "${useStandardLibary(withStandardLibrary)}\n\n${this.trimIndent()}"
        val module = compiler.compile(source)

        val moduleGenerator = ModuleGenerator()
        val watText = moduleGenerator.toSExpr(module)

        println(source)
        println()
        println(watText)

        val wat = File(temporaryFolder, "output.wat")
        wat.writeText(watText)

        val wasm = File(temporaryFolder, "output.wasm")
        val assembler = WebAssemblyAssembler()
        assembler.assemble(wat, wasm)

        val runner = WebAssemblyRunner()
        return runner.run(wasm.absolutePath)
    }
}