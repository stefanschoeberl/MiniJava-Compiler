package dev.ssch.util

import dev.ssch.minijava.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class CompilerTest {

    @TempDir
    lateinit var temporaryFolder: File

    fun String.runInMainFunction(withStandardLibrary: Boolean = true): String {
        return "public static void main() {\n${this.trimIndent().prependIndent("    ")}\n}".compileAndRunMainFunctionInMainClass(withStandardLibrary)
    }

    fun String.compileAndRunMainFunctionInMainClass(withStandardLibrary: Boolean = true): String {
        return "class Main {\n${this.trimIndent().prependIndent("    ")}\n}".compileAndRunMainFunction(withStandardLibrary)
    }

    fun String.compileAndRunMainFunction(withStandardLibrary: Boolean = true): String {
        val compiler = Compiler()
        val source = this.trimIndent()

        println(source)
        println()

        val testSourceFile = File(temporaryFolder, "main.minijava")
        testSourceFile.writeText(source)

        val wasmOutput = File(temporaryFolder, "wasm-output")

        val allSourceFiles = mutableListOf(testSourceFile)

        if (withStandardLibrary) {
            val stdlib = File(File(System.getProperty("user.dir")), "stdlib")
            stdlib.listFiles()!!
                .filter { it.isFile && it.name.endsWith(".minijava") }
                .forEach { allSourceFiles.add(it) }
        }

        val module = compiler.compile(allSourceFiles)

        val bundleGenerator = BundleGenerator()
        bundleGenerator.generateBundle(module, allSourceFiles, wasmOutput)

        val runner = WebAssemblyRunner()
        return runner.run(wasmOutput.absolutePath)
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