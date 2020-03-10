package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.UndefinedMethodException
import dev.ssch.minijava.exception.VoidParameterException
import dev.ssch.minijava.grammar.MiniJavaParser

class CallExpressionCodeGeneration(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.CallExprContext): DataType? {
        val parameterTypes = ctx.parameters.map {
            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(it)
        }

        val target = ctx.target

        val (className, methodName) = when (target) {
            is MiniJavaParser.IdExprContext -> Pair(codeGenerationPhase.currentClass, target.IDENT().text)
            is MiniJavaParser.MemberExprContext -> {
                val left = target.left
                if (left is MiniJavaParser.IdExprContext) {
                    Pair(left.IDENT().text, target.right.text)
                } else {
                    TODO("currently unsupported")
                }
            }
            else -> TODO("currently unsupported")
        }

        val parameters = parameterTypes.mapIndexed { index, type ->
            type ?: throw VoidParameterException(ctx.parameters[index].start)
        }

        if (!codeGenerationPhase.classSymbolTable.isDeclared(className)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val methodSymbolTableOfTargetClass = codeGenerationPhase.classSymbolTable.getMethodSymbolTable(className)

        if (!methodSymbolTableOfTargetClass.isDeclared(methodName, parameters)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val address = methodSymbolTableOfTargetClass.addressOf(methodName, parameters)

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(address))

        return methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameters)
    }
}