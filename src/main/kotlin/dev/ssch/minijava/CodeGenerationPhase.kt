package dev.ssch.minijava

import dev.ssch.minijava.ast.*
import dev.ssch.minijava.ast.Function
import dev.ssch.minijava.codegeneration.*
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

    val binaryExpressionCodeGenerator = BinaryExpressionCodeGenerator(this)
    val whileLoopCodeGenerator = WhileLoopStatementCodeGenerator(this)
    val ifElseLoopCodeGenerator = IfElseStatementCodeGenerator(this)
    val arrayCreationExpressionCodeGenerator = ArrayCreationExpressionCodeGenerator(this)
    val basicExpressionCodeGenerator = BasicExpressionCodeGenerator(this)
    val callExpressionCodeGenerator = CallExpressionCodeGeneration(this)
    val methodCodeGenerator = MethodCodeGenerator(this)
    val variableDeclarationStatementCodeGenerator = VariableDeclarationStatementCodeGenerator(this)
    val classInstanceCreationExpressionCodeGenerator = ClassInstanceCreationExpressionCodeGenerator(this)
    val arrayAccessExpressionCodeGeneration = ArrayAccessExpressionCodeGenerator(this)
    val variableAssignmentStatementCodeGenerator = VariableAssignmentStatementCodeGenerator(this)
    val basicStatementCodeGenerator = BasicStatementCodeGenerator(this)
    val memberAccessExpressionCodeGenerator = MemberExpressionCodeGenerator(this)
    val classCodeGenerator = ClassCodeGenerator(this)

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
        classCodeGenerator.generate(ctx)
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

    override fun visitVarassignStmt(ctx: MiniJavaParser.VarassignStmtContext) {
        variableAssignmentStatementCodeGenerator.generateExecution(ctx)
    }

    override fun visitExpressionStmt(ctx: MiniJavaParser.ExpressionStmtContext) {
        basicStatementCodeGenerator.generateExecution(ctx)
    }

    override fun visitReturnStmt(ctx: MiniJavaParser.ReturnStmtContext) {
        basicStatementCodeGenerator.generateExecution(ctx)
    }

    override fun visitArrayAccessExpr(ctx: MiniJavaParser.ArrayAccessExprContext) {
        arrayAccessExpressionCodeGeneration.generateEvaluation(ctx)
    }

    override fun visitMinusExpr(ctx: MiniJavaParser.MinusExprContext) {
        basicExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitCastExpr(ctx: MiniJavaParser.CastExprContext) {
        basicStatementCodeGenerator.generateExecution(ctx)
    }

    override fun visitCallExpr(ctx: MiniJavaParser.CallExprContext) {
        callExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitMemberExpr(ctx: MiniJavaParser.MemberExprContext) {
        memberAccessExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitIdExpr(ctx: MiniJavaParser.IdExprContext) {
        basicExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitIntExpr(ctx: MiniJavaParser.IntExprContext) {
        basicExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitBoolExpr(ctx: MiniJavaParser.BoolExprContext) {
        basicExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitFloatExpr(ctx: MiniJavaParser.FloatExprContext) {
        basicExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitParensExpr(ctx: MiniJavaParser.ParensExprContext) {
        basicExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitClassInstanceCreationExpr(ctx: MiniJavaParser.ClassInstanceCreationExprContext) {
        classInstanceCreationExpressionCodeGenerator.generate(ctx)
    }

    override fun visitArrayCreationExpr(ctx: MiniJavaParser.ArrayCreationExprContext) {
        arrayCreationExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitOrExpr(ctx: MiniJavaParser.OrExprContext) {
        binaryExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitAndExpr(ctx: MiniJavaParser.AndExprContext) {
        binaryExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitEqNeqExpr(ctx: MiniJavaParser.EqNeqExprContext) {
        binaryExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitRelationalExpr(ctx: MiniJavaParser.RelationalExprContext) {
        binaryExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitAddSubExpr(ctx: MiniJavaParser.AddSubExprContext) {
        binaryExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitMulDivExpr(ctx: MiniJavaParser.MulDivExprContext) {
        binaryExpressionCodeGenerator.generateEvaluation(ctx)
    }

    override fun visitBlockStmt(ctx: MiniJavaParser.BlockStmtContext) {
        basicStatementCodeGenerator.generateExecution(ctx)
    }

    override fun visitCompleteIfElseStmt(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
        ifElseLoopCodeGenerator.generateExecution(ctx)
    }

    override fun visitIncompleteIfStmt(ctx: MiniJavaParser.IncompleteIfStmtContext) {
        ifElseLoopCodeGenerator.generateExecution(ctx)
    }

    override fun visitIncompleteIfElseStmt(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
        ifElseLoopCodeGenerator.generateExecution(ctx)
    }

    override fun visitWhileLoopStmt(ctx: MiniJavaParser.WhileLoopStmtContext) {
        whileLoopCodeGenerator.generateExecution(ctx)
    }
}