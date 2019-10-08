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
    test("""
        int a;
        int A;
        int a1;
        int _abc;
    """.trimIndent(), "test3")
    test("""
        int a;

        println(2);
        a = 11;
        println(a);
        a = 33;
        println(a);

        int b;
        b = 44;
        println(b);
        b = a;
        println(b);
    """.trimIndent(), "test4")
    test("""
        int a;
        int b;
        a = 2;
        b = 3;
        int c;
        c = a + b;
        println(a);
        println(b);
        println(c);
    """.trimIndent(), "test4")
    test("""
        int a;
        int b;
        a = 2;
        b = 3;
        int c;
        c = b - a;
        println(a);
        println(b);
        println(c);
        c = a - b;
        println(c);
    """.trimIndent(), "test5")
    test("""
        int a;
        int b;
        int c;
        a = 3;
        b = 4;
        c = (a + a + a) - b;
        println(c);
        println((a + a + a) - b);
    """.trimIndent(), "test6")
    test("""
        int a = 1;
        println(1);
        int aa;
        int b = 2;
        int c = 3;
        println(a + b + c);
    """.trimIndent(), "test7")
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

        lateinit var symbolTable: SymbolTable

        override fun enterMinijava(ctx: MiniJavaParser.MinijavaContext?) {
            module = Module()
            symbolTable = SymbolTable()

            val printlnType = module.declareType(FuncType(mutableListOf(ValueType.I32), mutableListOf()))
            printlnAddr = module.importFunction(Import("imports", "println", ImportDesc.Func(printlnType)))

            val mainType = module.declareType(FuncType(mutableListOf(), mutableListOf()))
            mainFunction = Function(mainType, mutableListOf(), Expr(mutableListOf()))

            val main = module.declareFunction(mainFunction)
            module.exports.add(Export("main", ExportDesc.Func(main)))
        }

        override fun exitPrintln(ctx: MiniJavaParser.PrintlnContext) {
            mainFunction.body.instructions.add(Instruction.call(printlnAddr))
        }

        override fun exitVardeclassign(ctx: MiniJavaParser.VardeclassignContext) {
            val name = ctx.IDENT().text
            if (symbolTable.isDeclared(name)) {
                println("$name is already declared") // TODO emit semantic error
                return
            }
            symbolTable.declareVariable(name)
            mainFunction.locals.add(ValueType.I32)

            mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
        }

        override fun enterVardecl(ctx: MiniJavaParser.VardeclContext) {
            val name = ctx.IDENT().text
            if (symbolTable.isDeclared(name)) {
                println("$name is already declared") // TODO emit semantic error
                return
            }
            symbolTable.declareVariable(name)
            mainFunction.locals.add(ValueType.I32)
        }

        override fun exitVarassign(ctx: MiniJavaParser.VarassignContext) {
            val name = ctx.IDENT().text
            if (!symbolTable.isDeclared(name)) {
                println("$name is not declared") // TODO emit semantic error
                return
            }
            mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
        }

        override fun enterId(ctx: MiniJavaParser.IdContext) {
            val name = ctx.IDENT().text
            if (!symbolTable.isDeclared(name)) {
                println("$name is not declared") // TODO emit semantic error
                return
            }
            mainFunction.body.instructions.add(Instruction.local_get(symbolTable.addressOf(name)))
        }

        override fun enterInt(ctx: MiniJavaParser.IntContext) {
            mainFunction.body.instructions.add(Instruction.i32_const(ctx.INT().text.toInt()))
        }

        override fun exitAddSub(ctx: MiniJavaParser.AddSubContext) {
            when (ctx.op.type) {
                MiniJavaParser.PLUS -> mainFunction.body.instructions.add(Instruction.i32_add())
                MiniJavaParser.MINUS -> mainFunction.body.instructions.add(Instruction.i32_sub())
            }
        }
    }

    val walker = ParseTreeWalker()
    walker.walk(listener, tree)

    val text = ModuleWriter.toSExpr(listener.module)
    println(text)
    File("compilation/$output.wat").writeText(text)

    "wat2wasm compilation/$output.wat -o compilation/$output.wasm".runCommand(File(System.getProperty("user.dir")))
    "node run.js compilation/$output.wasm".runCommand(File(System.getProperty("user.dir")))

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