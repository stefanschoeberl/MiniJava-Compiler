package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.grammar.MiniJavaVisitor
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeProperty

class Visitor : AbstractParseTreeVisitor<Unit>(), MiniJavaVisitor<Unit> {

    lateinit var module: Module
    lateinit var mainFunction: Function
    var printlnIntAddr: Int = -1
    var printlnBoolAddr: Int = -1

    lateinit var symbolTable: SymbolTable
    lateinit var staticTypes: ParseTreeProperty<DataType>

    var ParseTree.staticType: DataType
        get() = staticTypes.get(this)
        set(type) = staticTypes.put(this, type)

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext) {
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

        visitChildren(ctx)

        symbolTable.allLocalVariables.forEach {
            when (it) {
                DataType.Integer -> mainFunction.locals.add(ValueType.I32)
                DataType.Boolean -> mainFunction.locals.add(ValueType.I32)
            }
        }

    }

    override fun visitPrintln(ctx: MiniJavaParser.PrintlnContext) {
        visit(ctx.expr())

        val address = when (ctx.expr().staticType) {
            DataType.Integer -> printlnIntAddr
            DataType.Boolean -> printlnBoolAddr
        }
        mainFunction.body.instructions.add(Instruction.call(address))
    }

    override fun visitVardeclassign(ctx: MiniJavaParser.VardeclassignContext) {
        visit(ctx.expr())

        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)

        if (type != ctx.expr().staticType) {
            throw IncompatibleAssignmentException(type, ctx.expr().staticType, ctx.name)
        }

        mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun visitVardecl(ctx: MiniJavaParser.VardeclContext) {
        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)
    }

    override fun visitVarassign(ctx: MiniJavaParser.VarassignContext) {
        visit(ctx.expr())

        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.name)
        }
        if (symbolTable.typeOf(name) != ctx.expr().staticType) {
            throw IncompatibleAssignmentException(symbolTable.typeOf(name), ctx.expr().staticType, ctx.name)
        }
        mainFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun visitMinusExpr(ctx: MiniJavaParser.MinusExprContext) {
        mainFunction.body.instructions.add(Instruction.i32_const(0))

        visit(ctx.expr())

        if (ctx.expr().staticType != DataType.Integer) {
            throw InvalidUnaryOperationException(ctx.expr().staticType, ctx.SUB().symbol)
        }
        mainFunction.body.instructions.add(Instruction.i32_sub())
        ctx.staticType = ctx.expr().staticType
    }

    override fun visitIdExpr(ctx: MiniJavaParser.IdExprContext) {
        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.IDENT().symbol)
        }
        mainFunction.body.instructions.add(Instruction.local_get(symbolTable.addressOf(name)))
        ctx.staticType = symbolTable.typeOf(name)
    }

    override fun visitIntExpr(ctx: MiniJavaParser.IntExprContext) {
        val value = ctx.INT().text.toInt()
        mainFunction.body.instructions.add(Instruction.i32_const(value))
        ctx.staticType = DataType.Integer
    }

    override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext) {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            mainFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            mainFunction.body.instructions.add(Instruction.i32_const(0))
        }
        ctx.staticType = DataType.Boolean
    }

    override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext) {
        visit(ctx.expr())

        ctx.staticType = ctx.expr().staticType
    }

    override fun visitOrExpr(ctx: MiniJavaParser.OrExprContext) {
        visit(ctx.left)
        visit(ctx.right)

        if (ctx.left.staticType != DataType.Boolean || ctx.right.staticType != DataType.Boolean) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        mainFunction.body.instructions.add(Instruction.i32_or())
        ctx.staticType = DataType.Boolean
    }

    override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext) {
        visit(ctx.left)
        visit(ctx.right)

        if (ctx.left.staticType != DataType.Boolean || ctx.right.staticType != DataType.Boolean) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        mainFunction.body.instructions.add(Instruction.i32_and())

        ctx.staticType = DataType.Boolean
    }

    override fun visitEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext) {
        visit(ctx.left)
        visit(ctx.right)

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

    override fun visitRelationalExpr(ctx: MiniJavaParser.RelationalExprContext) {
        visit(ctx.left)
        visit(ctx.right)

        if (ctx.left.staticType == DataType.Integer && ctx.right.staticType != DataType.Integer ||
            ctx.left.staticType == DataType.Boolean && ctx.right.staticType != DataType.Boolean) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        when (ctx.op.type) {
            MiniJavaParser.LT -> mainFunction.body.instructions.add(Instruction.i32_lt_s())
            MiniJavaParser.LE -> mainFunction.body.instructions.add(Instruction.i32_le_s())
            MiniJavaParser.GT -> mainFunction.body.instructions.add(Instruction.i32_gt_s())
            MiniJavaParser.GE -> mainFunction.body.instructions.add(Instruction.i32_ge_s())
        }

        ctx.staticType = DataType.Boolean
    }

    override fun visitAddSubExpr(ctx: MiniJavaParser.AddSubExprContext) {
        visit(ctx.left)
        visit(ctx.right)

        if (ctx.left.staticType != DataType.Integer || ctx.right.staticType != DataType.Integer) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        when (ctx.op.type) {
            MiniJavaParser.ADD -> mainFunction.body.instructions.add(Instruction.i32_add())
            MiniJavaParser.SUB -> mainFunction.body.instructions.add(Instruction.i32_sub())
        }
        ctx.staticType = DataType.Integer
    }

    override fun visitMulDivExpr(ctx: MiniJavaParser.MulDivExprContext) {
        visit(ctx.left)
        visit(ctx.right)

        if (ctx.left.staticType != DataType.Integer || ctx.right.staticType != DataType.Integer) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        when (ctx.op.type) {
            MiniJavaParser.MUL -> mainFunction.body.instructions.add(Instruction.i32_mul())
            MiniJavaParser.DIV -> mainFunction.body.instructions.add(Instruction.i32_div_s())
        }
        ctx.staticType = DataType.Integer
    }

    override fun visitBlock(ctx: MiniJavaParser.BlockContext) {
        symbolTable.pushScope()
        visitChildren(ctx)
        symbolTable.popScope()
    }

    override fun visitCompleteIfElse(ctx: MiniJavaParser.CompleteIfElseContext) {
        visitIfElse(ctx.condition, ctx.thenbranch, ctx.elsebranch)
    }

    override fun visitIncompleteIf(ctx: MiniJavaParser.IncompleteIfContext) {
        visitIfElse(ctx.condition, ctx.thenbranch)
    }

    override fun visitIncompleteIfElse(ctx: MiniJavaParser.IncompleteIfElseContext) {
        visitIfElse(ctx.condition, ctx.thenbranch, ctx.thenbranch)
    }

    fun visitIfElse(condition: MiniJavaParser.ExprContext, thenbranch: ParseTree, elsebranch: ParseTree? = null) {
        visit(condition)
        if (condition.staticType != DataType.Boolean) {
            throw IncompatibleTypeException(DataType.Boolean, condition.staticType, condition.getStart())
        }
        mainFunction.body.instructions.add(Instruction._if())
        visit(thenbranch)
        if (elsebranch != null) {
            mainFunction.body.instructions.add(Instruction._else())
            visit(elsebranch)
        }
        mainFunction.body.instructions.add(Instruction.end())
    }

    override fun visitStatement(ctx: MiniJavaParser.StatementContext) {
        visitChildren(ctx)
    }

    override fun visitWhileLoop(ctx: MiniJavaParser.WhileLoopContext) {
        with(mainFunction.body.instructions) {
            add(Instruction.block())
            add(Instruction.loop())

            visit(ctx.condition)
            if (ctx.condition.staticType != DataType.Boolean) {
                throw IncompatibleTypeException(DataType.Boolean, ctx.condition.staticType, ctx.condition.getStart())
            }
            add(Instruction.i32_eqz())
            add(Instruction.br_if(1))

            visit(ctx.statement())

            add(Instruction.br(0))

            add(Instruction.end())
            add(Instruction.end())
        }
    }
}