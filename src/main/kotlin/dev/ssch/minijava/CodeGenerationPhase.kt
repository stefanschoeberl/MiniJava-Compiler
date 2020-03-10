package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.codegeneration.*
import dev.ssch.minijava.exception.*
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.ClassSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable
import dev.ssch.minijava.symboltable.LocalVariableSymbolTable
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeProperty

class CodeGenerationPhase(val classSymbolTable: ClassSymbolTable) : MiniJavaBaseVisitor<Unit>() {

    lateinit var module: Module
    lateinit var currentClass: String
    lateinit var currentFunction: Function

    lateinit var localsVariableSymbolTable: LocalVariableSymbolTable
    lateinit var methodSymbolTable: MethodSymbolTable
    lateinit var staticTypes: ParseTreeProperty<DataType>

    lateinit var functions: MutableMap<Pair<String, MethodSymbolTable.MethodSignature>, Function>

    var ParseTree.staticType: DataType?
        get() = staticTypes.get(this)
        set(type) = staticTypes.put(this, type)

    val operatorTable = OperatorTable()

    var mallocAddress: Int = -1

    // ----------

    val binaryExpressionCodeGenerator = BinaryExpressionCodeGenerator(this)
    val whileLoopCodeGenerator = WhileLoopStatementCodeGenerator(this)
    val ifElseLoopCodeGenerator = IfElseStatementCodeGenerator(this)
    val arrayCreationExpressionCodeGenerator = ArrayCreationExpressionCodeGenerator(this)
    val basicExpressionCodeGenerator = BasicExpressionCodeGenerator(this)
    val callExpressionCodeGenerator = CallExpressionCodeGeneration(this)
    val methodCodeGenerator = MethodCodeGenerator(this)
    val variableDeclarationStatementCodeGenerator = VariableDeclarationStatementCodeGenerator(this)

    // ----------

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
        methodCodeGenerator.generate(ctx)
    }

    override fun visitVardeclassignStmt(ctx: MiniJavaParser.VardeclassignStmtContext) {
        variableDeclarationStatementCodeGenerator.generate(ctx)
    }

    override fun visitVardeclStmt(ctx: MiniJavaParser.VardeclStmtContext) {
        variableDeclarationStatementCodeGenerator.generate(ctx)
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

    private fun generateMemberExprAddressAndReturnResultingType(ctx: MiniJavaParser.MemberExprContext): DataType {
        visit(ctx.expr())
        val type = ctx.expr().staticType as? DataType.ReferenceType ?: TODO()
        val fieldName = ctx.right.text
        val fieldSymbolTable = classSymbolTable.getFieldSymbolTable(type.name)
        val fieldInfo = fieldSymbolTable.findFieldInfo(fieldName) ?: TODO()

        // add offset
        currentFunction.body.instructions.add(Instruction.i32_const(fieldInfo.offset))
        currentFunction.body.instructions.add(Instruction.i32_add)

        return fieldInfo.type
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
                if (!localsVariableSymbolTable.isDeclared(name)) {
                    throw UndefinedVariableException(name, left.IDENT().symbol)
                }
                checkAndConvertAssigment(localsVariableSymbolTable.typeOf(name))
                currentFunction.body.instructions.add(Instruction.local_set(localsVariableSymbolTable.addressOf(name)))
            }
            is MiniJavaParser.ArrayAccessExprContext -> {
                val elementType = generateArrayAddressCodeAndReturnElementType(left)

                visit(ctx.right)
                checkAndConvertAssigment(elementType)

                currentFunction.body.instructions.add(elementType.getStoreMemoryInstruction())

            }
            is MiniJavaParser.MemberExprContext -> {
                val type = generateMemberExprAddressAndReturnResultingType(left)

                visit(ctx.right)
                checkAndConvertAssigment(type)

                currentFunction.body.instructions.add(type.getStoreMemoryInstruction())

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
        basicExpressionCodeGenerator.generate(ctx)
    }

    override fun visitCastExpr(ctx: MiniJavaParser.CastExprContext) {
        val type = ctx.type.getDataType(classSymbolTable) ?: throw UnknownTypeException(ctx.type.text, ctx.type.start)

        visit(ctx.expr())

        val castCode = ctx.expr().staticType?.castTypeTo(type)
            ?: throw InconvertibleTypeException(ctx.expr().staticType, type, ctx.start)

        currentFunction.body.instructions.addAll(castCode)

        ctx.staticType = type
    }

    override fun visitCallExpr(ctx: MiniJavaParser.CallExprContext) {
        callExpressionCodeGenerator.generate(ctx)
    }

    override fun visitMemberExpr(ctx: MiniJavaParser.MemberExprContext) {
        val type = generateMemberExprAddressAndReturnResultingType(ctx)
        currentFunction.body.instructions.add(type.getLoadMemoryInstruction())
        ctx.staticType = type
    }

    override fun visitIdExpr(ctx: MiniJavaParser.IdExprContext) {
        basicExpressionCodeGenerator.generate(ctx)
    }

    override fun visitIntExpr(ctx: MiniJavaParser.IntExprContext) {
        basicExpressionCodeGenerator.generate(ctx)
    }

    override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext) {
        basicExpressionCodeGenerator.generate(ctx)
    }

    override fun visitFloatExpr(ctx: MiniJavaParser.FloatExprContext) {
        basicExpressionCodeGenerator.generate(ctx)
    }

    override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext) {
        basicExpressionCodeGenerator.generate(ctx)
    }

    override fun visitClassInstanceCreationExpr(ctx: MiniJavaParser.ClassInstanceCreationExprContext) {
        val type = (ctx.type as? MiniJavaParser.SimpleTypeContext)?.getDataType(classSymbolTable)
            as? DataType.ReferenceType // TODO
            ?: TODO()

        val size = classSymbolTable.getFieldSymbolTable(type.name).getSize()
        currentFunction.body.instructions.add(Instruction.i32_const(size))

        // allocate memory
        currentFunction.body.instructions.add(Instruction.call(mallocAddress))

        ctx.staticType = type
    }

    override fun visitArrayCreationExpr(ctx: MiniJavaParser.ArrayCreationExprContext) {
        arrayCreationExpressionCodeGenerator.generate(ctx)
    }

    override fun visitOrExpr(ctx: MiniJavaParser.OrExprContext) {
        binaryExpressionCodeGenerator.generateOrExpr(ctx)
    }

    override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext) {
        binaryExpressionCodeGenerator.generateAndExpr(ctx)
    }

    override fun visitEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext) {
        binaryExpressionCodeGenerator.generateEqNeqExpr(ctx)
    }

    override fun visitRelationalExpr(ctx: MiniJavaParser.RelationalExprContext) {
        binaryExpressionCodeGenerator.generateRelationalExpr(ctx)
    }

    override fun visitAddSubExpr(ctx: MiniJavaParser.AddSubExprContext) {
        binaryExpressionCodeGenerator.generateAddSubExpr(ctx)
    }

    override fun visitMulDivExpr(ctx: MiniJavaParser.MulDivExprContext) {
        binaryExpressionCodeGenerator.generateMulDivExpr(ctx)
    }

    override fun visitBlockStmt(ctx: MiniJavaParser.BlockStmtContext) {
        localsVariableSymbolTable.pushScope()
        visitChildren(ctx)
        localsVariableSymbolTable.popScope()
    }

    override fun visitCompleteIfElseStmt(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
        ifElseLoopCodeGenerator.generate(ctx)
    }

    override fun visitIncompleteIfStmt(ctx: MiniJavaParser.IncompleteIfStmtContext) {
        ifElseLoopCodeGenerator.generate(ctx)
    }

    override fun visitIncompleteIfElseStmt(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
        ifElseLoopCodeGenerator.generate(ctx)
    }

    override fun visitWhileLoopStmt(ctx: MiniJavaParser.WhileLoopStmtContext) {
        whileLoopCodeGenerator.generate(ctx)
    }
}