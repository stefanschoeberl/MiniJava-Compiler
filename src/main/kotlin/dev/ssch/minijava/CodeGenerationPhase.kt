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

    lateinit var functions: MutableMap<Pair<String, MethodSymbolTable.MethodSignature>, Function>

    val operatorTable = OperatorTable()

    var mallocAddress: Int = -1

    val classCodeGenerator = ClassCodeGenerator(this)
    val methodCodeGenerator = MethodCodeGenerator(this)

    val arrayAccessExpressionCodeGeneration = ArrayAccessExpressionCodeGenerator(this)
    val memberAccessExpressionCodeGenerator = MemberExpressionCodeGenerator(this)

    val expressionCodeGenerator = ExpressionCodeGenerator(this)
    val statementCodeGenerator = StatementCodeGenerator(this)

    override fun visitMinijava(ctx: MiniJavaParser.MinijavaContext) {
        module = Module()
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
}