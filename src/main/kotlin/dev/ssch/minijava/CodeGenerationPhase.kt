package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeProperty

class CodeGenerationPhase(private val classSymbolTable: ClassSymbolTable) : MiniJavaBaseVisitor<Unit>() {

    lateinit var module: Module
    private lateinit var currentClass: String
    private lateinit var currentFunction: Function

    private lateinit var symbolTable: SymbolTable
    private lateinit var methodSymbolTable: MethodSymbolTable
    private lateinit var staticTypes: ParseTreeProperty<DataType>

    private lateinit var functions: MutableMap<Pair<String, MethodSymbolTable.MethodSignature>, Function>

    private var ParseTree.staticType: DataType?
        get() = staticTypes.get(this)
        set(type) = staticTypes.put(this, type)

    private val operatorTable = OperatorTable()

    private var mallocAddress: Int = -1

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext) {
        module = Module()
        staticTypes = ParseTreeProperty()
        functions = mutableMapOf()

        fun declareFunctionType(signature: MethodSymbolTable.MethodSignature, information: MethodSymbolTable.MethodInformation): Int {
            if (information.isStatic) {
                val parameters = signature.parameterTypes.map { type -> type.toWebAssemblyType() }.toMutableList()
                val returnType = information.returnType
                    ?.let { type -> mutableListOf(type.toWebAssemblyType()) }
                    ?: mutableListOf()
                return module.declareType(FuncType(parameters, returnType))
            } else {
                TODO()
            }
        }

        mallocAddress = module.importFunction(Import("internal", "malloc",
            ImportDesc.Func(declareFunctionType(
                MethodSymbolTable.MethodSignature("malloc", listOf(DataType.PrimitiveType.Integer)),
                MethodSymbolTable.MethodInformation(-1, DataType.PrimitiveType.Integer,
                    isPublic = false,
                    isStatic = true
                )
            ))))

        classSymbolTable.classes.flatMap { classEntry ->
            classEntry.value.methodSymbolTable.nativeMethods.map {
                Pair(classEntry.key, it)
            }
        }.sortedBy { it.second.value.address }.forEach {
            val className = it.first
            val methodSignature = it.second.key
            val methodInfo = it.second.value

            val functionType = declareFunctionType(methodSignature, methodInfo)
            module.importFunction(Import(
                "imports",
                "$className.${methodSignature.externalName()}",
                ImportDesc.Func(functionType)))
        }

        classSymbolTable.classes
            .flatMap { classEntry ->
                classEntry.value.methodSymbolTable.methods.entries.map { Pair(classEntry.key, it) }
            }
            .sortedBy { it.second.value.address }
            .forEach {
                val className = it.first
                val methodSignature = it.second.key
                val methodInfo = it.second.value

                val functionType = declareFunctionType(methodSignature, methodInfo)
                val function = Function(functionType, mutableListOf(), Expr(mutableListOf()))
                module.declareFunction(function)
                functions[Pair(className, methodSignature)] = function
                if (methodInfo.isPublic) {
                    module.exports.add(Export(
                        "$className.${methodSignature.externalName()}",
                        ExportDesc.Func(methodInfo.address)))
                }
            }

        visitChildren(ctx)

        module.imports.add(Import("internal", "memory", ImportDesc.Memory(MemType(1))))
    }

    override fun visitJavaclass(ctx: MiniJavaParser.JavaclassContext) {
        currentClass = ctx.name.text
        methodSymbolTable = classSymbolTable.getMethodSymbolTable(currentClass)
        visitChildren(ctx)
    }

    override fun visitMethod(ctx: MiniJavaParser.MethodContext) {
        if (ctx.nativemodifier.isNotEmpty()) {
            return
        }

        // reset scope
        symbolTable = SymbolTable()

        val parameters = ctx.parameters.map { Pair(it.name.text, it.type.getDataType()!!) }
        val parameterTypes = parameters.map { it.second }
        currentFunction = functions[Pair(currentClass, MethodSymbolTable.MethodSignature(ctx.name.text, parameterTypes))]!!

        parameters.forEach {
            symbolTable.declareParameter(it.first, it.second)
        }

        ctx.statements.forEach {
            visit(it)
        }

        // "workaround" idea from https://github.com/WebAssembly/wabt/issues/1075
        if (methodSymbolTable.returnTypeOf(ctx.name.text, parameters.map { it.second }) != null) {
            currentFunction.body.instructions.add(Instruction.unreachable)
        }

        symbolTable.allLocalVariables.forEach {
            currentFunction.locals.add(it.toWebAssemblyType())
        }
    }

    override fun visitVardeclassignStmt(ctx: MiniJavaParser.VardeclassignStmtContext) {
        visit(ctx.expr())

        val name = ctx.name.text

        val type = ctx.type.getDataType()
            ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)

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
        val type = ctx.type.getDataType() ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)
        if (symbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, ctx.name)
        }
        symbolTable.declareVariable(name, type)
    }

    fun generateArrayAddressCodeAndReturnElementType(ctx: MiniJavaParser.ArrayAccessExprContext): DataType {
        // address = arraystart + itemsize * index + 4
        visit(ctx.array)
        val arrayType = ctx.array.staticType as? DataType.Array ?: throw ExpressionIsNotAnArrayException(ctx.array.start)
        visit(ctx.index)
        if (ctx.index.staticType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, ctx.index.staticType, ctx.index.start)
        }
        currentFunction.body.instructions.add(Instruction.i32_const(arrayType.elementType.sizeInBytes()))

        currentFunction.body.instructions.add(Instruction.i32_mul)
        currentFunction.body.instructions.add(Instruction.i32_add)

        // skip size part
        currentFunction.body.instructions.add(Instruction.i32_const(4))
        currentFunction.body.instructions.add(Instruction.i32_add)

        return arrayType.elementType
    }

    override fun visitVarassignStmt(ctx: MiniJavaParser.VarassignStmtContext) {

        fun checkAndConvertAssigment(leftType: DataType?) {
            val conversionCode = leftType?.let {
                ctx.right.staticType?.assignTypeTo(it)
            } ?: throw IncompatibleAssignmentException(leftType, ctx.right.staticType, ctx.left.start)

            currentFunction.body.instructions.addAll(conversionCode)
        }

        when (val left = ctx.left) {
            is MiniJavaParser.IdExprContext -> {
                visit(ctx.right)
                val name = left.IDENT().text
                if (!symbolTable.isDeclared(name)) {
                    throw UndefinedVariableException(name, left.IDENT().symbol)
                }
                checkAndConvertAssigment(symbolTable.typeOf(name))
                currentFunction.body.instructions.add(Instruction.local_set(symbolTable.addressOf(name)))
            }
            is MiniJavaParser.ArrayAccessExprContext -> {
                val elementType = generateArrayAddressCodeAndReturnElementType(left)

                visit(ctx.right)
                checkAndConvertAssigment(elementType)

                currentFunction.body.instructions.add(elementType.getStoreMemoryInstruction())

            }
            else -> throw InvalidAssignmentException(left.start)
        }
    }

    override fun visitExpressionStmt(ctx: MiniJavaParser.ExpressionStmtContext) {
        if (ctx.expr() is MiniJavaParser.CallExprContext) {
            visit(ctx.expr())

            if (ctx.expr().staticType != null) {
                currentFunction.body.instructions.add(Instruction.drop)
            }
        } else {
            TODO()
        }
    }

    override fun visitReturnStmt(ctx: MiniJavaParser.ReturnStmtContext) {
        visit(ctx.value)

        currentFunction.body.instructions.add(Instruction._return)
    }

    override fun visitArrayAccessExpr(ctx: MiniJavaParser.ArrayAccessExprContext) {
        val elementType = generateArrayAddressCodeAndReturnElementType(ctx)
        currentFunction.body.instructions.add(elementType.getLoadMemoryInstruction())
        ctx.staticType = elementType
    }

    override fun visitMinusExpr(ctx: MiniJavaParser.MinusExprContext) {
        val codePositionBeforeOperand = currentFunction.body.instructions.size
        visit(ctx.expr())

        val type = ctx.expr().staticType
        val unaryOperation = operatorTable.findUnaryMinusOperation(type)
            ?: throw InvalidUnaryOperationException(ctx.expr().staticType, ctx.SUB().symbol)

        currentFunction.body.instructions.add(codePositionBeforeOperand, unaryOperation.operationBeforeOperand)
        currentFunction.body.instructions.add(unaryOperation.operationAfterOperand)

        ctx.staticType = type
    }

    override fun visitCastExpr(ctx: MiniJavaParser.CastExprContext) {
        val type = ctx.type.getDataType() ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)

        visit(ctx.expr())

        val castCode = ctx.expr().staticType?.castTypeTo(type)
            ?: throw InconvertibleTypeException(ctx.expr().staticType, type, ctx.start)

        currentFunction.body.instructions.addAll(castCode)

        ctx.staticType = type
    }

    override fun visitCallExpr(ctx: MiniJavaParser.CallExprContext) {
        ctx.parameters.forEach {
            visit(it)
        }

        val target = ctx.target

        val (className, methodName) = when (target) {
            is MiniJavaParser.IdExprContext -> Pair(currentClass, target.IDENT().text)
            is MiniJavaParser.MemberExprContext -> {
                val left = target.left
                val right = target.right

                if (left is MiniJavaParser.IdExprContext && right is MiniJavaParser.IdExprContext) {
                    Pair(left.IDENT().text, right.IDENT().text)
                } else {
                    TODO("currently unsupported")
                }
            }
            else -> TODO("currently unsupported")
        }

        val parameters = ctx.parameters.map {
            it.staticType ?: throw VoidParameterException(it.start)
        }

        if (!classSymbolTable.isDeclared(className)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val methodSymbolTableOfTargetClass = classSymbolTable.getMethodSymbolTable(className)

        if (!methodSymbolTableOfTargetClass.isDeclared(methodName, parameters)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val address = methodSymbolTableOfTargetClass.addressOf(methodName, parameters)

        currentFunction.body.instructions.add(Instruction.call(address))

        ctx.staticType = methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameters)
    }

    override fun visitMemberExpr(ctx: MiniJavaParser.MemberExprContext?) {
        TODO("currently unsupported")
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
        ctx.staticType = DataType.PrimitiveType.Integer
    }

    override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext) {
        if (ctx.value.type == MiniJavaParser.TRUE) {
            currentFunction.body.instructions.add(Instruction.i32_const(1))
        } else {
            currentFunction.body.instructions.add(Instruction.i32_const(0))
        }
        ctx.staticType = DataType.PrimitiveType.Boolean
    }

    override fun visitFloatExpr(ctx: MiniJavaParser.FloatExprContext) {
        val value = ctx.FLOAT().text.toFloat()
        currentFunction.body.instructions.add(Instruction.f32_const(value))
        ctx.staticType = DataType.PrimitiveType.Float
    }

    override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext) {
        visit(ctx.expr())

        ctx.staticType = ctx.expr().staticType
    }

    override fun visitArrayCreationExpr(ctx: MiniJavaParser.ArrayCreationExprContext) {
        visit(ctx.size)
        if (ctx.size.staticType != DataType.PrimitiveType.Integer) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Integer, ctx.size.staticType, ctx.size.start)
        }
        val arrayType = (ctx.type as? MiniJavaParser.PrimitiveTypeContext)?.getDataType()
            ?: TODO()

        val sizeVariable = if (symbolTable.isDeclared("#size")) {
            symbolTable.addressOf("#size")
        } else {
            symbolTable.declareVariable("#size", DataType.PrimitiveType.Integer)
        }

        // store array size in #size (no dup)
        // https://github.com/WebAssembly/design/issues/1102
        currentFunction.body.instructions.add(Instruction.local_tee(sizeVariable))

        currentFunction.body.instructions.add(Instruction.i32_const(arrayType.sizeInBytes()))
        currentFunction.body.instructions.add(Instruction.i32_mul)

        // 4 extra bytes for array size
        currentFunction.body.instructions.add(Instruction.i32_const(4))
        currentFunction.body.instructions.add(Instruction.i32_add)

        // allocate memory
        currentFunction.body.instructions.add(Instruction.call(mallocAddress))

        val arrayAddressVariable = if (symbolTable.isDeclared("#array")) {
            symbolTable.addressOf("#array")
        } else {
            symbolTable.declareVariable("#array", DataType.PrimitiveType.Integer)
        }

        // store array address in #array
        currentFunction.body.instructions.add(Instruction.local_tee(arrayAddressVariable))

        // store array size in first 4 bytes
        currentFunction.body.instructions.add(Instruction.local_get(sizeVariable))
        currentFunction.body.instructions.add(Instruction.i32_store)

        // put array address on top of stack
        currentFunction.body.instructions.add(Instruction.local_get(arrayAddressVariable))

        ctx.staticType = DataType.Array(arrayType)
    }

    private fun visitBinaryOperatorExpression(ctx: MiniJavaParser.ExprContext, left: MiniJavaParser.ExprContext, right: MiniJavaParser.ExprContext, op: Token) {
        visit(left)
        val codePositionAfterLeftOperand = currentFunction.body.instructions.size
        visit(right)

        val binaryOperation = operatorTable.findBinaryOperation(left.staticType, right.staticType, op)
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
        if (condition.staticType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Boolean, condition.staticType, condition.getStart())
        }
        currentFunction.body.instructions.add(Instruction._if)
        visit(thenbranch)
        if (elsebranch != null) {
            currentFunction.body.instructions.add(Instruction._else)
            visit(elsebranch)
        }
        currentFunction.body.instructions.add(Instruction.end)
    }

    override fun visitWhileLoopStmt(ctx: MiniJavaParser.WhileLoopStmtContext) {
        with(currentFunction.body.instructions) {
            add(Instruction.block)
            add(Instruction.loop)

            visit(ctx.condition)
            if (ctx.condition.staticType != DataType.PrimitiveType.Boolean) {
                throw IncompatibleTypeException(DataType.PrimitiveType.Boolean, ctx.condition.staticType, ctx.condition.getStart())
            }
            add(Instruction.i32_eqz)
            add(Instruction.br_if(1))

            visit(ctx.body)

            add(Instruction.br(0))

            add(Instruction.end)
            add(Instruction.end)
        }
    }
}