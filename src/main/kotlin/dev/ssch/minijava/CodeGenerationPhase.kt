package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token
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

    private val operatorTable = OperatorTable()

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
                DataType.Float -> currentFunction.locals.add(ValueType.F32)
            }
        }
    }

    override fun visitVardeclassignStmt(ctx: MiniJavaParser.VardeclassignStmtContext) {
        visit(ctx.expr())

        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)

        val conversionCode = ctx.expr().staticType!!.assignTypeTo(type)
            ?: throw IncompatibleAssignmentException(type, ctx.expr().staticType, ctx.name)

        currentFunction.body.instructions.addAll(conversionCode)

        currentFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun visitVardeclStmt(ctx: MiniJavaParser.VardeclStmtContext) {
        val name = ctx.name.text
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)
    }

    override fun visitVarassignStmt(ctx: MiniJavaParser.VarassignStmtContext) {
        visit(ctx.expr())

        val name = ctx.IDENT().text
        if (!symbolTable.isDeclared(name)) {
            throw UndefinedVariableException(name, ctx.name)
        }

        val conversionCode = ctx.expr().staticType!!.assignTypeTo(symbolTable.typeOf(name))
            ?: throw IncompatibleAssignmentException(symbolTable.typeOf(name), ctx.expr().staticType, ctx.name)

        currentFunction.body.instructions.addAll(conversionCode)

        currentFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
    }

    override fun visitCallStmt(ctx: MiniJavaParser.CallStmtContext) {
        visit(ctx.callExpression())

        if (ctx.callExpression().staticType != null) {
            currentFunction.body.instructions.add(Instruction.drop())
        }
    }

    override fun visitReturnStmt(ctx: MiniJavaParser.ReturnStmtContext) {
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
        val codePositionBeforeOperand = currentFunction.body.instructions.size
        visit(ctx.expr())

        val type = ctx.expr().staticType
        if (type?.isNumeric() != true) {
            throw InvalidUnaryOperationException(ctx.expr().staticType, ctx.SUB().symbol)
        }

        when (type) {
            DataType.Integer -> {
                currentFunction.body.instructions.add(codePositionBeforeOperand, Instruction.i32_const(0))
                currentFunction.body.instructions.add(Instruction.i32_sub())
            }
            DataType.Float -> {
                currentFunction.body.instructions.add(codePositionBeforeOperand, Instruction.f32_const(0f))
                currentFunction.body.instructions.add(Instruction.f32_sub())
            }
            DataType.Boolean -> throw RuntimeException("Should not happen")
        }

        ctx.staticType = type
    }

    override fun visitCastExpr(ctx: MiniJavaParser.CastExprContext) {
        val type = DataType.fromString(ctx.type.text) ?: throw UnknownTypeException(ctx.type.text, ctx.type)

        visit(ctx.expr())

        val castCode = ctx.expr().staticType?.castTypeTo(type)
            ?: throw InconvertibleTypeException(ctx.expr().staticType, type, ctx.start)

        currentFunction.body.instructions.addAll(castCode)

        ctx.staticType = type
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

    override fun visitFloatExpr(ctx: MiniJavaParser.FloatExprContext) {
        val value = ctx.FLOAT().text.toFloat()
        currentFunction.body.instructions.add(Instruction.f32_const(value))
        ctx.staticType = DataType.Float
    }

    override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext) {
        visit(ctx.expr())

        ctx.staticType = ctx.expr().staticType
    }

    private fun visitBinaryOperatorExpression(ctx: MiniJavaParser.ExprContext, left: MiniJavaParser.ExprContext, right: MiniJavaParser.ExprContext, op: Token) {
        visit(left)
        val codePositionAfterLeftOperand = currentFunction.body.instructions.size
        visit(right)

        val binaryOperation = operatorTable.findOperation(left.staticType, right.staticType, op)
            ?: throw InvalidBinaryOperationException(left.staticType, right.staticType, op)

        binaryOperation.leftPromotion?.let { currentFunction.body.instructions.add(codePositionAfterLeftOperand, it) }
        binaryOperation.rightPromotion?.let { currentFunction.body.instructions.add(it) }

        currentFunction.body.instructions.add(binaryOperation.operation)

        ctx.staticType = binaryOperation.resultingType
    }

    override fun visitOrExpr(ctx: MiniJavaParser.OrExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    override fun visitEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    override fun visitRelationalExpr(ctx: MiniJavaParser.RelationalExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    override fun visitAddSubExpr(ctx: MiniJavaParser.AddSubExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    override fun visitMulDivExpr(ctx: MiniJavaParser.MulDivExprContext) {
        visitBinaryOperatorExpression(ctx, ctx.left, ctx.right, ctx.op)
    }

    override fun visitBlockStmt(ctx: MiniJavaParser.BlockStmtContext) {
        symbolTable.pushScope()
        visitChildren(ctx)
        symbolTable.popScope()
    }

    override fun visitCompleteIfElseStmt(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
        visitIfElse(ctx.condition, ctx.thenbranch, ctx.elsebranch)
    }

    override fun visitIncompleteIfStmt(ctx: MiniJavaParser.IncompleteIfStmtContext) {
        visitIfElse(ctx.condition, ctx.thenbranch)
    }

    override fun visitIncompleteIfElseStmt(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
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

    override fun visitWhileLoopStmt(ctx: MiniJavaParser.WhileLoopStmtContext) {
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