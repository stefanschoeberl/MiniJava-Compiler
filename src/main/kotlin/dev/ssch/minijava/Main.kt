package dev.ssch.minijava

import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("file missing")
        return
    }

    val input = args[0]
    val parts = input.split(".")

    if (parts.size != 2 || parts[1] != "minijava") {
        println("not a MiniJava file")
        return
    }

    val name = parts[0]

    val compiler = Compiler()
    val module = compiler.compile(File(input).readText())

    val moduleGenerator = ModuleGenerator()
    val watText = moduleGenerator.toSExpr(module)

    println(watText)

    val wat = File("$name.wat")
    wat.writeText(watText)

    val wasm = File("$name.wasm")
    val assembler = WebAssemblyAssembler()
    assembler.assemble(wat, wasm)
}