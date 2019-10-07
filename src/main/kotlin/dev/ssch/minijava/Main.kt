package dev.ssch.minijava

import dev.ssch.minijava.grammar.MiniJavaBaseListener
import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    test("""
        println(1);
    """.trimIndent(), "test1")
    test("""
        println(1);
        println(2);
    """.trimIndent(), "test2")
}

fun test(src: String, output: String) {
    val input = CharStreams.fromString(src)
    val lexer = MiniJavaLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MiniJavaParser(tokens)
    val tree = parser.minijava()

    val listener = object : MiniJavaBaseListener() {

        // https://developer.mozilla.org/en-US/docs/WebAssembly/Understanding_the_text_format

        val result = StringBuilder()

        override fun enterMinijava(ctx: MiniJavaParser.MinijavaContext?) {
            result.clear()
            result.append("(module (import \"imports\" \"println\" (func (param i32))) ")
//            result.append("(module (func \$println (import \"imports\" \"println\") (param i32)) ")
            result.append("(func ")
        }

        override fun exitMinijava(ctx: MiniJavaParser.MinijavaContext?) {
            result.append(") ")
            result.append("(export \"main\" (func 1)) ")
            result.append(")")
            println(result)
        }

        override fun enterPrintlnstatement(ctx: MiniJavaParser.PrintlnstatementContext) {
            result.append("i32.const ${ctx.INT().text} call 0 ")
        }

        fun compileAndRun() {
            File("compilation/$output.wat").writeText(result.toString())

            "wat2wasm compilation/$output.wat -o compilation/$output.wasm".runCommand(File(System.getProperty("user.dir")))
            "node run.js compilation/$output.wasm".runCommand(File(System.getProperty("user.dir")))
        }
    }
    val walker = ParseTreeWalker()
    walker.walk(listener, tree)
    listener.compileAndRun()
    println("-----")
}

// https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}