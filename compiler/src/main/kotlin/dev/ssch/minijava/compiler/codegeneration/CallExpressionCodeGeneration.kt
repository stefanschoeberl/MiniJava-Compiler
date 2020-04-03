package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.UndefinedMethodException
import dev.ssch.minijava.compiler.exception.UndefinedVariableException
import dev.ssch.minijava.compiler.exception.VoidParameterException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class CallExpressionCodeGeneration (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val basicExpressionCodeGenerator: BasicExpressionCodeGenerator
) {

    fun generateEvaluation(ctx: MiniJavaParser.CallExprContext): DataType? {
        val (className, generatedTargetReferenceOnStack) = extractTargetClassNameAndGenerateTargetReference(ctx)

        val codePositionBeforeParameters = codeEmitter.nextInstructionAddress
        val parameterTypes = evaluateParameters(ctx)

        val methodName = extractMethodName(ctx)

        if (!codeEmitter.classSymbolTable.isDeclared(className)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val methodSymbolTableOfTargetClass = codeEmitter.classSymbolTable.getMethodSymbolTable(className)

        if (!methodSymbolTableOfTargetClass.isDeclared(methodName, parameterTypes)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val address = methodSymbolTableOfTargetClass.addressOf(methodName, parameterTypes)

        if (methodSymbolTableOfTargetClass.isStatic(methodName, parameterTypes) && generatedTargetReferenceOnStack) {
            // remove reference to target object
            codeEmitter.emitInstruction(codePositionBeforeParameters, Instruction.drop)
        }

        codeEmitter.emitInstruction(Instruction.call(address))

        return methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameterTypes)
    }

    private fun extractTargetClassNameAndGenerateTargetReference(ctx: MiniJavaParser.CallExprContext): Pair<String, Boolean> {
        val target = ctx.target
        val startCodePosition = codeEmitter.nextInstructionAddress
        if (target is MiniJavaParser.MemberExprContext) {
            return when (val left = target.left) {
                is MiniJavaParser.IdExprContext -> try {
                    // try to evaluate identifier as variable
                    Pair((basicExpressionCodeGenerator.generateEvaluation(left) as? DataType.ReferenceType)?.name
                        ?: TODO("call not on object"), true)
                } catch (e: UndefinedVariableException) {
                    // if evaluation fails, try to interpret identifier as class name

                    codeEmitter.deleteInstructionsBeginningAt(startCodePosition) // remove previous evaluation
                    val className = left.IDENT().text
                    if (codeEmitter.classSymbolTable.isDeclared(className)) {
                        Pair(className, false)
                    } else {
                        TODO("not a class")
                    }
                }
                else -> Pair((expressionCodeGenerator.generateEvaluation(target.left) as? DataType.ReferenceType)?.name
                    ?: TODO("call not on object"), true)
            }
        } else if (target is MiniJavaParser.IdExprContext) {
            return Pair(codeEmitter.currentClass, false)
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
            expressionCodeGenerator.generateEvaluation(it)
        }
        return parameterTypes.mapIndexed { index, type ->
            type ?: throw VoidParameterException(ctx.parameters[index].start)
        }
    }
}