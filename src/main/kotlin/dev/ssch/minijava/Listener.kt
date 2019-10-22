package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.grammar.MiniJavaBaseListener
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.tree.ParseTreeProperty

class Listener : MiniJavaBaseListener() {

    // https://developer.mozilla.org/en-US/docs/WebAssembly/Understanding_the_text_format

    lateinit var module: Module
    lateinit var mainFunction: Function
    var printlnIntAddr: Int = -1
    var printlnBoolAddr: Int = -1

    lateinit var symbolTable: SymbolTable
    lateinit var staticTypes: ParseTreeProperty<DataType>

    override fun enterMinijava(ctx: MiniJavaParser.MinijavaContext?) {
        module = Module()
        symbolTable = SymbolTable()
        staticTypes = ParseTreeProperty()

        val printlnType = module.declareType(FuncType(mutableListOf(ValueType.I32), mutableListOf()))
        printlnIntAddr = module.importFunction(Import("imports", "println_int", ImportDesc.Func(printlnType)))
        printlnBoolAddr = module.importFunction(Import("imports", "println_bool", ImportDesc.Func(printlnType)))

        val mainType = module.declareType(FuncType(mutableListOf(), mutableListOf()))
        mainFunction = Function(mainType, mutableListOf(), Expr(mutableListOf()))

        val main = module.declareFunction(mainFunction)
        module.exports.add(Export("main", ExportDesc.Func(main)))
    }

    override fun exitPrintln(ctx: MiniJavaParser.PrintlnContext) {
        val address = when (staticTypes.get(ctx.expr())) { // TODO remove nullability
            DataType.Integer -> printlnIntAddr
            DataType.Boolean -> printlnBoolAddr
        }
        mainFunction.body.instructions.add(Instruction.call(address))
    }

    override fun exitVardeclassign(ctx: MiniJavaParser.VardeclassignContext) {
        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text)
        if (symbolTable.isDeclared(name)) {
            println("$name is already declared") // TODO emit semantic error
            return
        }
        symbolTable.declareVariable(name, type)
        mainFunction.locals.add(ValueType.I32)

        if (type != staticTypes.get(ctx.expr())) {
            println("cannot assign variable") // TODO emit semantic error
            return
        }

        mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun enterVardecl(ctx: MiniJavaParser.VardeclContext) {
        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text)
        if (symbolTable.isDeclared(name)) {
            println("$name is already declared") // TODO emit semantic error
            return
        }
        symbolTable.declareVariable(name, type)
        mainFunction.locals.add(ValueType.I32)
    }

    override fun exitVarassign(ctx: MiniJavaParser.VarassignContext) {
        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            println("$name is not declared") // TODO emit semantic error
            return
        }
        if (symbolTable.typeOf(name) != staticTypes.get(ctx.expr())) {
            println("cannot assign variable") // TODO emit semantic error
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
        staticTypes.put(ctx, symbolTable.typeOf(name))
    }

    override fun enterInt(ctx: MiniJavaParser.IntContext) {
        val value = if (ctx.SUB() != null) {
            -ctx.INT().text.toInt()
        } else {
            ctx.INT().text.toInt()
        }
        mainFunction.body.instructions.add(Instruction.i32_const(value))
        staticTypes.put(ctx, DataType.Integer)
    }

    override fun enterBool(ctx: MiniJavaParser.BoolContext) {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            mainFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            mainFunction.body.instructions.add(Instruction.i32_const(0))
        }
        staticTypes.put(ctx, DataType.Boolean)
    }

    override fun enterParens(ctx: MiniJavaParser.ParensContext) {
        if (ctx.SUB() != null) {
            mainFunction.body.instructions.add(Instruction.i32_const(0))
        }
    }

    override fun exitParens(ctx: MiniJavaParser.ParensContext) {
        if (ctx.SUB() != null) { // TODO check types of operands
            mainFunction.body.instructions.add(Instruction.i32_sub())
        }
        staticTypes.put(ctx, staticTypes.get(ctx.expr()))
    }

    override fun exitEqNeq(ctx: MiniJavaParser.EqNeqContext) {
        mainFunction.body.instructions.add(Instruction.i32_eq())
        staticTypes.put(ctx, DataType.Boolean) // TODO check types of operands
    }

    override fun exitAddSub(ctx: MiniJavaParser.AddSubContext) {
        when (ctx.op.type) {
            MiniJavaParser.ADD -> mainFunction.body.instructions.add(Instruction.i32_add())
            MiniJavaParser.SUB -> mainFunction.body.instructions.add(Instruction.i32_sub())
        }
        staticTypes.put(ctx, DataType.Integer) // TODO check types of operands
    }

    override fun exitMulDiv(ctx: MiniJavaParser.MulDivContext) {
        when (ctx.op.type) {
            MiniJavaParser.MUL -> mainFunction.body.instructions.add(Instruction.i32_mul())
            MiniJavaParser.DIV -> mainFunction.body.instructions.add(Instruction.i32_div_s())
        }
        staticTypes.put(ctx, DataType.Integer) // TODO check types of operands
    }

}