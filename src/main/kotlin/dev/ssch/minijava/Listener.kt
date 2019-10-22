package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.grammar.MiniJavaBaseListener
import dev.ssch.minijava.grammar.MiniJavaParser

class Listener : MiniJavaBaseListener() {

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
        symbolTable.declareVariable(name, DataType.Integer)
        mainFunction.locals.add(ValueType.I32)

        mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun enterVardecl(ctx: MiniJavaParser.VardeclContext) {
        val name = ctx.IDENT().text
        if (symbolTable.isDeclared(name)) {
            println("$name is already declared") // TODO emit semantic error
            return
        }
        symbolTable.declareVariable(name, DataType.Integer)
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
        if (ctx.SUB() != null) {
            mainFunction.body.instructions.add(Instruction.i32_const(0))
        }
        mainFunction.body.instructions.add(Instruction.local_get(symbolTable.addressOf(name)))
        if (ctx.SUB() != null) {
            mainFunction.body.instructions.add(Instruction.i32_sub())
        }
    }

    override fun enterInt(ctx: MiniJavaParser.IntContext) {
        val value = if (ctx.SUB() != null) {
            -ctx.INT().text.toInt()
        } else {
            ctx.INT().text.toInt()
        }
        mainFunction.body.instructions.add(Instruction.i32_const(value))
    }

    override fun enterParens(ctx: MiniJavaParser.ParensContext) {
        if (ctx.SUB() != null) {
            mainFunction.body.instructions.add(Instruction.i32_const(0))
        }
    }

    override fun exitParens(ctx: MiniJavaParser.ParensContext) {
        if (ctx.SUB() != null) {
            mainFunction.body.instructions.add(Instruction.i32_sub())
        }
    }

    override fun exitAddSub(ctx: MiniJavaParser.AddSubContext) {
        when (ctx.op.type) {
            MiniJavaParser.ADD -> mainFunction.body.instructions.add(Instruction.i32_add())
            MiniJavaParser.SUB -> mainFunction.body.instructions.add(Instruction.i32_sub())
        }
    }

    override fun exitMulDiv(ctx: MiniJavaParser.MulDivContext) {
        when (ctx.op.type) {
            MiniJavaParser.MUL -> mainFunction.body.instructions.add(Instruction.i32_mul())
            MiniJavaParser.DIV -> mainFunction.body.instructions.add(Instruction.i32_div_s())
        }
    }

}