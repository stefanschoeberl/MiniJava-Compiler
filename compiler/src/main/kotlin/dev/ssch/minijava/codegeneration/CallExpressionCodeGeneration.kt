package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.UndefinedMethodException
import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.minijava.exception.VoidParameterException
import dev.ssch.minijava.grammar.MiniJavaParser

class CallExpressionCodeGeneration(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.CallExprContext): DataType? {
        val (className, generatedTargetReferenceOnStack) = extractTargetClassNameAndGenerateTargetReference(ctx)

        val codePositionBeforeParameters = codeGenerationPhase.currentFunction.body.instructions.size
        val parameterTypes = evaluateParameters(ctx)

        val methodName = extractMethodName(ctx)

        if (!codeGenerationPhase.classSymbolTable.isDeclared(className)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val methodSymbolTableOfTargetClass = codeGenerationPhase.classSymbolTable.getMethodSymbolTable(className)

        if (!methodSymbolTableOfTargetClass.isDeclared(methodName, parameterTypes)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val address = methodSymbolTableOfTargetClass.addressOf(methodName, parameterTypes)

        if (methodSymbolTableOfTargetClass.isStatic(methodName, parameterTypes) && generatedTargetReferenceOnStack) {
            // remove reference to target object
            codeGenerationPhase.currentFunction.body.instructions.add(codePositionBeforeParameters, Instruction.drop)
        }

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(address))

        return methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameterTypes)
    }

    private fun removeInstructionsBeginningAt(n: Int) {
        val instructions = codeGenerationPhase.currentFunction.body.instructions
        while (instructions.size > n) {
            instructions.removeAt(instructions.size - 1)
        }

    }

    private fun extractTargetClassNameAndGenerateTargetReference(ctx: MiniJavaParser.CallExprContext): Pair<String, Boolean> {
        val target = ctx.target
        val startCodePosition = codeGenerationPhase.currentFunction.body.instructions.size
        if (target is MiniJavaParser.MemberExprContext) {
            return when (val left = target.left) {
                is MiniJavaParser.IdExprContext -> try {
                    // try to evaluate identifier as variable
                    Pair((codeGenerationPhase.basicExpressionCodeGenerator.generateEvaluation(left) as? DataType.ReferenceType)?.name
                        ?: TODO("call not on object"), true)
                } catch (e: UndefinedVariableException) {
                    // if evaluation fails, try to interpret identifier as class name

                    removeInstructionsBeginningAt(startCodePosition) // remove previous evaluation
                    val className = left.IDENT().text
                    if (codeGenerationPhase.classSymbolTable.isDeclared(className)) {
                        Pair(className, false)
                    } else {
                        TODO("not a class")
                    }
                }
                else -> Pair((codeGenerationPhase.expressionCodeGenerator.generateEvaluation(target.left) as? DataType.ReferenceType)?.name
                    ?: TODO("call not on object"), true)
            }
        } else if (target is MiniJavaParser.IdExprContext) {
            return Pair(codeGenerationPhase.currentClass, false)
        } else {
            TODO("call on unsupported expression")
        }
    }

    private fun extractMethodName(ctx: MiniJavaParser.CallExprContext): String {
        return when (val target = ctx.target) {
            is MiniJavaParser.IdExprContext -> target.IDENT().text
            is MiniJavaParser.MemberExprContext -> target.right.text
            else -> TODO("call on unsupported expression")
        }
    }

    private fun evaluateParameters(ctx: MiniJavaParser.CallExprContext): List<DataType> {
        val parameterTypes = ctx.parameters.map {
            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(it)
        }
        return parameterTypes.mapIndexed { index, type ->
            type ?: throw VoidParameterException(ctx.parameters[index].start)
        }
    }
}