package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.grammar.MiniJavaBaseListener
import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File
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

        lateinit var module: Module
        lateinit var mainFunction: Function
        var printlnAddr: Int = -1

        override fun enterMinijava(ctx: MiniJavaParser.MinijavaContext?) {
            module = Module()

            val printlnType = module.declareType(FuncType(mutableListOf(ValueType.I32), mutableListOf()))
            printlnAddr = module.importFunction(Import("imports", "println", ImportDesc.Func(printlnType)))

            val mainType = module.declareType(FuncType(mutableListOf(), mutableListOf()))
            mainFunction = Function(mainType, mutableListOf(), Expr(mutableListOf()))

            val main = module.declareFunction(mainFunction)
            module.exports.add(Export("main", ExportDesc.Func(main)))
        }

        override fun enterPrintlnstatement(ctx: MiniJavaParser.PrintlnstatementContext) {
            mainFunction.body.instructions.apply {
                add(Instruction.I32_const(ctx.INT().text.toInt()))
                add(Instruction.Call(printlnAddr))
            }
        }

        fun compileAndRun() {
            val text = ModuleWriter.toSExpr(module)
            println(text)
            File("compilation/$output.wat").writeText(text)

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