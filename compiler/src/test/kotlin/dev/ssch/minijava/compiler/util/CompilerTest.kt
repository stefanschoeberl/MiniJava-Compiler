package dev.ssch.minijava.compiler.util

import dev.ssch.minijava.compiler.CompilationContext
import dev.ssch.minijava.wasm.WebAssemblyRunner
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class CompilerTest {

    class Source (miniJava: String) {

        var miniJava: String = miniJava
            private set

        var javaScript: String? = null
            private set

        companion object {
            fun withMiniJava(src: String): Source {
                return Source(src)
            }
        }

        fun andJavaScript(src: String): Source {
            javaScript = src
            return this
        }

        fun wrapInMainClass(): Source {
            miniJava = "class Main {\n${miniJava.trimIndent().prependIndent("    ")}\n}"
            return this
        }

        fun wrapInMainFunction(): Source {
            miniJava = "public static void main() {\n${miniJava.trimIndent().prependIndent("    ")}\n}"
            return this
        }
    }

    @TempDir
    lateinit var temporaryFolder: File

    fun String.compileAndRunInMainFunction(withStandardLibrary: Boolean = true): String {
        return Source.withMiniJava(this).wrapInMainFunction().compileAndRunInMainClass(withStandardLibrary)
    }

    // ---

    fun Source.compileAndRunInMainClass(withStandardLibrary: Boolean = true): String {
        return this.wrapInMainClass().compileAndRun(withStandardLibrary)
    }

    fun String.compileAndRunInMainClass(withStandardLibrary: Boolean = true): String {
        return Source.withMiniJava(this).wrapInMainClass().compileAndRun(withStandardLibrary)
    }

    // ---

    fun Source.compileAndRun(withStandardLibrary: Boolean = true): String {
        val context = CompilationContext()
        val miniJavaSource = this.miniJava.trimIndent()
        val javaScriptSource = this.javaScript?.trimIndent()

        println(miniJavaSource)
        println()

        val testSourceFile = File(temporaryFolder, "main.minijava")
        testSourceFile.writeText(miniJavaSource)

        if (javaScriptSource != null) {
            File(temporaryFolder, "main.js").writeText(javaScriptSource)
        }

        val wasmOutput = File(temporaryFolder, "wasm-output")

        val allSourceFiles = mutableListOf(testSourceFile)

        if (withStandardLibrary) {
            val stdlib = File(File(System.getProperty("user.dir")).parentFile, "stdlib")
            stdlib.listFiles()!!
                .filter { it.isFile && it.name.endsWith(".minijava") }
                .forEach { allSourceFiles.add(it) }
        }

        val (module, classSymbolTable) = context.compiler.compile(allSourceFiles)

        context.bundleGenerator.generateBundle(module, allSourceFiles, wasmOutput, classSymbolTable)

        val runner = WebAssemblyRunner()
        return runner.run(wasmOutput.absolutePath)
    }

    fun String.compileAndRun(withStandardLibrary: Boolean = true): String {
        return Source.withMiniJava(this).compileAndRun(withStandardLibrary)
    }

    fun v(value: String): (String) -> Unit = { actual ->
        Assertions.assertThat(actual).isEqualTo(value)
    }

    fun v(value: Int): (String) -> Unit = { actual ->
        Assertions.assertThat(actual.toInt()).isEqualTo(value)
    }

    fun v(value: Boolean): (String) -> Unit = { actual ->
        Assertions.assertThat(actual.toBoolean()).isEqualTo(value)
    }

    fun v(value: Float, precision: Float): (String) -> Unit = { actual ->
        Assertions.assertThat(actual.toFloat()).isEqualTo(value, within(precision))
    }

    fun List<String>.matches(vararg assertions: (String) -> Unit) {
        this.zip(assertions).forEach { it.second(it.first) }
    }
}