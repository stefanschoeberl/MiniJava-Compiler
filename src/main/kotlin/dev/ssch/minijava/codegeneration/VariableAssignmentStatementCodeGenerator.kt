package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.assignTypeTo
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.IncompatibleAssignmentException
import dev.ssch.minijava.exception.InvalidAssignmentException
import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.minijava.getStoreMemoryInstruction
import dev.ssch.minijava.grammar.MiniJavaParser

class VariableAssignmentStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.VarassignStmtContext) {
        fun checkAndConvertAssigment(leftType: DataType?, rightType: DataType?) {
            val conversionCode = leftType?.let {
                rightType?.assignTypeTo(it)
            } ?: throw IncompatibleAssignmentException(leftType, rightType, ctx.left.start)

            codeGenerationPhase.currentFunction.body.instructions.addAll(conversionCode)
        }

        when (val left = ctx.left) {
            is MiniJavaParser.IdExprContext -> {
                val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.right)
                val name = left.IDENT().text
                if (!codeGenerationPhase.localsVariableSymbolTable.isDeclared(name)) {
                    throw UndefinedVariableException(name, left.IDENT().symbol)
                }
                checkAndConvertAssigment(codeGenerationPhase.localsVariableSymbolTable.typeOf(name), rightType)
                codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_set(codeGenerationPhase.localsVariableSymbolTable.addressOf(name)))
            }
            is MiniJavaParser.ArrayAccessExprContext -> {
                val elementType = codeGenerationPhase.arrayAccessExpressionCodeGeneration.generateElementAddressCodeAndReturnElementType(left)

                val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.right)
                checkAndConvertAssigment(elementType, rightType)

                codeGenerationPhase.currentFunction.body.instructions.add(elementType.getStoreMemoryInstruction())

            }
            is MiniJavaParser.MemberExprContext -> {
                val resultingType = codeGenerationPhase.memberAccessExpressionCodeGenerator.generateMemberExprAddressAndReturnResultingType(left)

                val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.right)
                checkAndConvertAssigment(resultingType, rightType)

                codeGenerationPhase.currentFunction.body.instructions.add(resultingType.getStoreMemoryInstruction())

            }
            else -> throw InvalidAssignmentException(left.start)
        }
    }
}