package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeProperty

class CodeGenerationPhase(private val methodSymbolTable: MethodSymbolTable) : MiniJavaBaseVisitor<Unit>() {

    lateinit var module: Module
    private lateinit var currentFunction: Function

    private lateinit var symbolTable: SymbolTable
    private lateinit var staticTypes: ParseTreeProperty<DataType>

    private lateinit var functions: MutableMap<MethodSymbolTable.MethodSignature, Function>

    private var ParseTree.staticType: DataType?
        get() = staticTypes.get(this)
        set(type) = staticTypes.put(this, type)

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext) {
        module = Module()
        staticTypes = ParseTreeProperty()
        functions = mutableMapOf()

        fun declareFunctionType(signature: MethodSymbolTable.MethodSignature, information: MethodSymbolTable.MethodInformation): Int {
            val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
            val returnType = information.returnType
                ?.let { type -> mutableListOf(type.toWebAssemblyType()) }
                ?: mutableListOf()
           return module.declareType(FuncType(parameters, returnType))
        }

        methodSymbolTable.nativeMethods.entries.sortedBy { it.value.address }.forEach {
            val functionType = declareFunctionType(it.key, it.value)
            module.importFunction(Import("imports", it.key.externalName(), ImportDesc.Func(functionType)))
        }

        methodSymbolTable.methods.entries.sortedBy { it.value.address }.forEach {
            val functionType = declareFunctionType(it.key, it.value)
            val function = Function(functionType, mutableListOf(), Expr(mutableListOf()))
            functions[it.key] = function
            val functionAddress = module.declareFunction(function)
            if (it.value.isPublic) {
                module.exports.add(Export(it.key.externalName(), ExportDesc.Func(functionAddress)))
            }
        }

        visitChildren(ctx)
    }

    override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
        if (ctx.nativemodifier.isNotEmpty()) {
            return
        }

        // reset scope
        symbolTable = SymbolTable()

        val parameters = ctx.parameters.map { Pair(it.name.text, DataType.fromString(it.type.text)!!) }
        val parameterTypes = parameters.map { it.second }
        currentFunction = functions[MethodSymbolTable.MethodSignature(ctx.name.text, parameterTypes)]!!

        parameters.forEach {
            symbolTable.declareParameter(it.first, it.second)
        }

        ctx.statements.forEach {
            visit(it)
        }

        // "workaround" idea from https://github.com/WebAssembly/wabt/issues/1075
        if (methodSymbolTable.returnTypeOf(ctx.name.text, parameters.map { it.second }) != null) {
            currentFunction.body.instructions.add(Instruction.unreachable())
        }

        symbolTable.allLocalVariables.forEach {
            when (it) {
                DataType.Integer -> currentFunction.locals.add(ValueType.I32)
                DataType.Boolean -> currentFunction.locals.add(ValueType.I32)
            }
        }
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

        currentFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
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
        currentFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun visitCall(ctx: MiniJavaParser.CallContext) {
        visit(ctx.callExpression())

        if (ctx.callExpression().staticType != null) {
            currentFunction.body.instructions.add(Instruction.drop())
        }
    }

    override fun visitReturn(ctx: MiniJavaParser.ReturnContext) {
        visit(ctx.value)

        currentFunction.body.instructions.add(Instruction._return())
    }

    override fun visitCallExpression(ctx: MiniJavaParser.CallExpressionContext) {
        ctx.parameters.forEach {
            visit(it)
        }

        val name = ctx.name.text
        val parameters = ctx.parameters.map {
            it.staticType ?: throw VoidParameterException(it.start)
        }

        if (!methodSymbolTable.isDeclared(name, parameters)) {
            throw UndefinedMethodException(name, ctx.name)
        }

        val address = methodSymbolTable.addressOf(name, parameters)

        currentFunction.body.instructions.add(Instruction.call(address))

        ctx.staticType = methodSymbolTable.returnTypeOf(name, parameters)
    }

    override fun visitMinusExpr(ctx: MiniJavaParser.MinusExprContext) {
        currentFunction.body.instructions.add(Instruction.i32_const(0))

        visit(ctx.expr())

        if (ctx.expr().staticType != DataType.Integer) {
            throw InvalidUnaryOperationException(ctx.expr().staticType, ctx.SUB().symbol)
        }
        currentFunction.body.instructions.add(Instruction.i32_sub())
        ctx.staticType = ctx.expr().staticType
    }

    override fun visitCallExpr(ctx: MiniJavaParser.CallExprContext) {
        visit(ctx.callExpression())

        ctx.staticType = ctx.callExpression().staticType
    }

    override fun visitIdExpr(ctx: MiniJavaParser.IdExprContext) {
        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.IDENT().symbol)
        }
        currentFunction.body.instructions.add(Instruction.local_get(symbolTable.addressOf(name)))
        ctx.staticType = symbolTable.typeOf(name)
    }

    override fun visitIntExpr(ctx: MiniJavaParser.IntExprContext) {
        val value = ctx.INT().text.toInt()
        currentFunction.body.instructions.add(Instruction.i32_const(value))
        ctx.staticType = DataType.Integer
    }

    override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext) {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            currentFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            currentFunction.body.instructions.add(Instruction.i32_const(0))
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

        currentFunction.body.instructions.add(Instruction.i32_or())
        ctx.staticType = DataType.Boolean
    }

    override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext) {
        visit(ctx.left)
        visit(ctx.right)

        if (ctx.left.staticType != DataType.Boolean || ctx.right.staticType != DataType.Boolean) {
            throw InvalidBinaryOperationException(ctx.left.staticType, ctx.right.staticType, ctx.op)
        }

        currentFunction.body.instructions.add(Instruction.i32_and())

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
            MiniJavaParser.EQ -> currentFunction.body.instructions.add(Instruction.i32_eq())
            MiniJavaParser.NEQ -> currentFunction.body.instructions.add(Instruction.i32_ne())
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
            MiniJavaParser.LT -> currentFunction.body.instructions.add(Instruction.i32_lt_s())
            MiniJavaParser.LE -> currentFunction.body.instructions.add(Instruction.i32_le_s())
            MiniJavaParser.GT -> currentFunction.body.instructions.add(Instruction.i32_gt_s())
            MiniJavaParser.GE -> currentFunction.body.instructions.add(Instruction.i32_ge_s())
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
            MiniJavaParser.ADD -> currentFunction.body.instructions.add(Instruction.i32_add())
            MiniJavaParser.SUB -> currentFunction.body.instructions.add(Instruction.i32_sub())
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
            MiniJavaParser.MUL -> currentFunction.body.instructions.add(Instruction.i32_mul())
            MiniJavaParser.DIV -> currentFunction.body.instructions.add(Instruction.i32_div_s())
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
        currentFunction.body.instructions.add(Instruction._if())
        visit(thenbranch)
        if (elsebranch != null) {
            currentFunction.body.instructions.add(Instruction._else())
            visit(elsebranch)
        }
        currentFunction.body.instructions.add(Instruction.end())
    }

    override fun visitWhileLoop(ctx: MiniJavaParser.WhileLoopContext) {
        with(currentFunction.body.instructions) {
            add(Instruction.block())
            add(Instruction.loop())

            visit(ctx.condition)
            if (ctx.condition.staticType != DataType.Boolean) {
                throw IncompatibleTypeException(DataType.Boolean, ctx.condition.staticType, ctx.condition.getStart())
            }
            add(Instruction.i32_eqz())
            add(Instruction.br_if(1))

            visit(ctx.body)

            add(Instruction.br(0))

            add(Instruction.end())
            add(Instruction.end())
        }
    }
}