package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.*
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

        if (!methodSymbolTableOfTargetClass.isCallable(methodName, parameterTypes)) {
            throw UndefinedMethodException("$className.$methodName", ctx.target.start)
        }

        val address = methodSymbolTableOfTargetClass.addressOf(methodName, parameterTypes)

        if (methodSymbolTableOfTargetClass.isStatic(methodName, parameterTypes) && generatedTargetReferenceOnStack) {
            // remove reference to target object
            codeEmitter.emitInstruction(codePositionBeforeParameters, Instruction.drop)
        }

        if (!methodSymbolTableOfTargetClass.isStatic(methodName, parameterTypes) && !generatedTargetReferenceOnStack) {
            if (!codeEmitter.localsVariableSymbolTable.doesThisParameterExist()) {
                throw InstanceMethodCallFromStaticMethodException(className, methodName, ctx.target.stop)
            } else {
                val thisAddress = codeEmitter.localsVariableSymbolTable.addressOfThis()
                codeEmitter.emitInstruction(codePositionBeforeParameters, Instruction.local_get(thisAddress))
            }
        }

        codeEmitter.emitInstruction(Instruction.call(address))

        return methodSymbolTableOfTargetClass.returnTypeOf(methodName, parameterTypes)
    }

    private fun extractTargetClassNameAndGenerateTargetReference(ctx: MiniJavaParser.CallExprContext): Pair<String, Boolean> {
        val target = ctx.target
        val startCodePosition = codeEmitter.nextInstructionAddress
        if (target is MiniJavaParser.MemberExprContext) {
            return when (val left = target.left) {
                is MiniJavaParser.IdExprContext -> {
                    fun tryToInterpretIdentiferAsClassName(): Pair<String, Boolean> {
                        codeEmitter.deleteInstructionsBeginningAt(startCodePosition) // remove previous evaluation
                        val className = left.IDENT().text
                        if (codeEmitter.classSymbolTable.isDeclared(className)) {
                            return Pair(className, false)
                        } else {
                            throw UndefinedClassException(className, left.start)
                        }
                    }

                    try {
                        // try to evaluate identifier as variable
                        val leftRawType = basicExpressionCodeGenerator.generateEvaluation(left)
                        val leftType = leftRawType as? DataType.ReferenceType
                            ?: throw NotAReferenceTypeException(leftRawType, left.start)

                        Pair(leftType.name, true)
                    } catch (e: UndefinedVariableException) {
                        // if evaluation fails, try to interpret identifier as class name
                        tryToInterpretIdentiferAsClassName()
                    } catch (e: UndefinedFieldException) {
                        // if evaluation fails, try to interpret identifier as class name
                        tryToInterpretIdentiferAsClassName()
                    }
                }
                else -> {
                    val leftRawType = expressionCodeGenerator.generateEvaluation(target.left)
                    val leftType = leftRawType as? DataType.ReferenceType
                        ?: throw NotAReferenceTypeException(leftRawType, left.start)

                    Pair(leftType.name, true)
                }
            }
        } else if (target is MiniJavaParser.IdExprContext) {
            return Pair(codeEmitter.currentClass, false)
        } else {
            throw NotACallableExpressionException(ctx.target.text, ctx.target.start)
        }
    }

    private fun extractMethodName(ctx: MiniJavaParser.CallExprContext): String {
        return when (val target = ctx.target) {
            is MiniJavaParser.IdExprContext -> target.IDENT().text
            is MiniJavaParser.MemberExprContext -> target.right.text
            else -> throw NotACallableExpressionException(ctx.target.text, ctx.target.start)
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