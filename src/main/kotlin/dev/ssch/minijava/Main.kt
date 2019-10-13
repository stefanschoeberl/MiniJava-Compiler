package dev.ssch.minijava

import dev.ssch.minijava.grammar.MiniJavaLexer
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.File

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

    val listener = Listener()

    val walker = ParseTreeWalker()
    walker.walk(listener, tree)

    val text = ModuleGenerator().toSExpr(listener.module)
    println(text)
    val wat = File("compilation/$output.wat")
    wat.writeText(text)
    val wasm = File("compilation/$output.wasm")
    WebAssemblyAssembler().assemble(wat, wasm)

    WebAssemblyRunner().run(wasm.absolutePath)

    println("-----")
}