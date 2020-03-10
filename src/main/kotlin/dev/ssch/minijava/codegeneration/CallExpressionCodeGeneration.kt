package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.UndefinedMethodException
import dev.ssch.minijava.exception.VoidParameterException
import dev.ssch.minijava.grammar.MiniJavaParser

class CallExpressionCodeGeneration(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.CallExprContext) {
        ctx.parameters.forEach {
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

        val parameters = ctx.parameters.map {
            it.staticType ?: throw VoidParameterException(it.start)
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

        ctx.staticType = methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameters)
    }
}