package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.UndefinedMethodException
import dev.ssch.minijava.compiler.exception.UndefinedVariableException
import dev.ssch.minijava.compiler.exception.VoidParameterException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class CallExpressionCodeGeneration(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.CallExprContext): DataType? {
        val (className, generatedTargetReferenceOnStack) = extractTargetClassNameAndGenerateTargetReference(ctx)

        val codePositionBeforeParameters = codeGenerationPhase.nextInstructionAddress
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
            codeGenerationPhase.emitInstruction(codePositionBeforeParameters, Instruction.drop)
        }

        codeGenerationPhase.emitInstruction(Instruction.call(address))

        return methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameterTypes)
    }

    private fun extractTargetClassNameAndGenerateTargetReference(ctx: MiniJavaParser.CallExprContext): Pair<String, Boolean> {
        val target = ctx.target
        val startCodePosition = codeGenerationPhase.nextInstructionAddress
        if (target is MiniJavaParser.MemberExprContext) {
            return when (val left = target.left) {
                is MiniJavaParser.IdExprContext -> try {
                    // try to evaluate identifier as variable
                    Pair((codeGenerationPhase.basicExpressionCodeGenerator.generateEvaluation(left) as? DataType.ReferenceType)?.name
                        ?: TODO("call not on object"), true)
                } catch (e: UndefinedVariableException) {
                    // if evaluation fails, try to interpret identifier as class name

                    codeGenerationPhase.deleteInstructionsBeginningAt(startCodePosition) // remove previous evaluation
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