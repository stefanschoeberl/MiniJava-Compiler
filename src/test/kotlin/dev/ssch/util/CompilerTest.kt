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
        """ else ""
    }

    fun String.runInMainFunction(withStandardLibrary: Boolean = true): String {
        return "public void main() { $this }".compileAndRunMainFunction(withStandardLibrary)
    }

    fun String.compileAndRunMainFunction(withStandardLibrary: Boolean = true): String {
        val compiler = Compiler()
        val module = compiler.compile("${useStandardLibary(withStandardLibrary)} $this" )

        val moduleGenerator = ModuleGenerator()
        val watText = moduleGenerator.toSExpr(module)

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