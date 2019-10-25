package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseListener
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeProperty

class Listener : MiniJavaBaseListener() {

    // https://developer.mozilla.org/en-US/docs/WebAssembly/Understanding_the_text_format

    lateinit var module: Module
    lateinit var mainFunction: Function
    var printlnIntAddr: Int = -1
    var printlnBoolAddr: Int = -1

    lateinit var symbolTable: SymbolTable
    lateinit var staticTypes: ParseTreeProperty<DataType>

    var ParseTree.staticType: DataType
        get() = staticTypes.get(this)
        set(type) = staticTypes.put(this, type)

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
        val address = when (ctx.expr().staticType) {
            DataType.Integer -> printlnIntAddr
            DataType.Boolean -> printlnBoolAddr
        }
        mainFunction.body.instructions.add(Instruction.call(address))
    }

    override fun exitVardeclassign(ctx: MiniJavaParser.VardeclassignContext) {
        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)
        mainFunction.locals.add(ValueType.I32)

        if (type != ctx.expr().staticType) {
            throw IncompatibleAssignmentException(type, ctx.expr().staticType, ctx.name)
        }

        mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun enterVardecl(ctx: MiniJavaParser.VardeclContext) {
        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)
        mainFunction.locals.add(ValueType.I32)
    }

    override fun exitVarassign(ctx: MiniJavaParser.VarassignContext) {
        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.name)
        }
        if (symbolTable.typeOf(name) != ctx.expr().staticType) {
            throw IncompatibleAssignmentException(symbolTable.typeOf(name), ctx.expr().staticType, ctx.name)
        }
        mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun enterMinus(ctx: MiniJavaParser.MinusContext?) {
        mainFunction.body.instructions.add(Instruction.i32_const(0))
    }

    override fun exitMinus(ctx: MiniJavaParser.MinusContext) {
        if (ctx.expr().staticType != DataType.Integer) {
            throw InvalidUnaryOperationException(ctx.expr().staticType, ctx.SUB().symbol)
        }
        mainFunction.body.instructions.add(Instruction.i32_sub())
        ctx.staticType = ctx.expr().staticType
    }

    override fun enterId(ctx: MiniJavaParser.IdContext) {
        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.IDENT().symbol)
        }
        mainFunction.body.instructions.add(Instruction.local_get(symbolTable.addressOf(name)))
        ctx.staticType = symbolTable.typeOf(name)
    }

    override fun enterInt(ctx: MiniJavaParser.IntContext) {
        val value = ctx.INT().text.toInt()
        mainFunction.body.instructions.add(Instruction.i32_const(value))
        ctx.staticType = DataType.Integer
    }

    override fun enterBool(ctx: MiniJavaParser.BoolContext) {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            mainFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            mainFunction.body.instructions.add(Instruction.i32_const(0))
        }
        ctx.staticType = DataType.Boolean
    }

    override fun exitParens(ctx: MiniJavaParser.ParensContext) {
        ctx.staticType = ctx.expr().staticType
    }

    override fun exitEqNeq(ctx: MiniJavaParser.EqNeqContext) {
        if (ctx.left.staticType == DataType.Integer && ctx.right.staticType != DataType.Integer ||
            ctx.left.staticType == DataType.Boolean && ctx.right.staticType != DataType.Boolean) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        when (ctx.op.type) {
            MiniJavaParser.EQ -> mainFunction.body.instructions.add(Instruction.i32_eq())
            MiniJavaParser.NEQ -> mainFunction.body.instructions.add(Instruction.i32_ne())
        }

        ctx.staticType = DataType.Boolean
    }

    override fun exitAddSub(ctx: MiniJavaParser.AddSubContext) {
        if (ctx.left.staticType != DataType.Integer || ctx.right.staticType != DataType.Integer) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        when (ctx.op.type) {
            MiniJavaParser.ADD -> mainFunction.body.instructions.add(Instruction.i32_add())
            MiniJavaParser.SUB -> mainFunction.body.instructions.add(Instruction.i32_sub())
        }
        ctx.staticType = DataType.Integer
    }

    override fun exitMulDiv(ctx: MiniJavaParser.MulDivContext) {
        if (ctx.left.staticType != DataType.Integer || ctx.right.staticType != DataType.Integer) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        when (ctx.op.type) {
            MiniJavaParser.MUL -> mainFunction.body.instructions.add(Instruction.i32_mul())
            MiniJavaParser.DIV -> mainFunction.body.instructions.add(Instruction.i32_div_s())
        }
        ctx.staticType = DataType.Integer
    }

}