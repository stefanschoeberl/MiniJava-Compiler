package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.assignTypeTo
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.IncompatibleAssignmentException
import dev.ssch.minijava.exception.InvalidAssignmentException
import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class VariableAssignmentStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun checkAndConvertAssigment(leftType: DataType?, rightType: DataType?, token: Token) {
        val conversionCode = leftType?.let {
            rightType?.assignTypeTo(it)
        } ?: throw IncompatibleAssignmentException(leftType, rightType, token)

        codeGenerationPhase.currentFunction.body.instructions.addAll(conversionCode)
    }

    fun generateExecution(ctx: MiniJavaParser.VarassignStmtContext) {
        when (val left = ctx.left) {
            is MiniJavaParser.IdExprContext -> {
                val name = left.IDENT().text

                val instructions = codeGenerationPhase.currentFunction.body.instructions
                val localsVariableSymbolTable = codeGenerationPhase.localsVariableSymbolTable

                when {
                    localsVariableSymbolTable.isDeclared(name) -> {
                        val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.right)
                        checkAndConvertAssigment(codeGenerationPhase.localsVariableSymbolTable.typeOf(name), rightType, ctx.left.start)
                        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_set(codeGenerationPhase.localsVariableSymbolTable.addressOf(name)))
                    }
                    localsVariableSymbolTable.doesThisParameterExist() -> {
                        codeGenerationPhase.memberAccessExpressionCodeGenerator.generateWrite(name, ctx.right, ctx.left.start) {
                            instructions.add(Instruction.local_get(localsVariableSymbolTable.addressOfThis()))
                            DataType.ReferenceType(codeGenerationPhase.currentClass)
                        }
                    }
                    else -> throw UndefinedVariableException(name, left.IDENT().symbol)

                }
            }
            is MiniJavaParser.ArrayAccessExprContext -> {
                val elementType = codeGenerationPhase.arrayAccessExpressionCodeGeneration.generateArrayAndIndexAddressesAndReturnElementType(left)

                val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.right)
                checkAndConvertAssigment(elementType, rightType, ctx.left.start)

                val address = when (elementType) {
                    DataType.PrimitiveType.Integer -> codeGenerationPhase.setArrayPrimitiveIntAddress
                    DataType.PrimitiveType.Float -> codeGenerationPhase.setArrayPrimitiveFloatAddress
                    DataType.PrimitiveType.Boolean -> codeGenerationPhase.setArrayPrimitiveBooleanAddress
                    DataType.PrimitiveType.Char -> codeGenerationPhase.setArrayPrimitiveCharAddress
                    is DataType.ReferenceType -> codeGenerationPhase.setArrayReferenceAddress
                    else -> TODO()
                }

                codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(address))
            }
            is MiniJavaParser.MemberExprContext -> {
                codeGenerationPhase.memberAccessExpressionCodeGenerator.generateWrite(left, ctx.right)
            }
            else -> throw InvalidAssignmentException(left.start)
        }
    }
}