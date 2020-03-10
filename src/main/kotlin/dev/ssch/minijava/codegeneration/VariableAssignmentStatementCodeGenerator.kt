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

class VariableAssignmentStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.VarassignStmtContext) {
        fun checkAndConvertAssigment(leftType: DataType?) {
            val conversionCode = leftType?.let {
                ctx.right.staticType?.assignTypeTo(it)
            } ?: throw IncompatibleAssignmentException(leftType, ctx.right.staticType, ctx.left.start)

            codeGenerationPhase.currentFunction.body.instructions.addAll(conversionCode)
        }

        when (val left = ctx.left) {
            is MiniJavaParser.IdExprContext -> {
                codeGenerationPhase.visit(ctx.right)
                val name = left.IDENT().text
                if (!codeGenerationPhase.localsVariableSymbolTable.isDeclared(name)) {
                    throw UndefinedVariableException(name, left.IDENT().symbol)
                }
                checkAndConvertAssigment(codeGenerationPhase.localsVariableSymbolTable.typeOf(name))
                codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_set(codeGenerationPhase.localsVariableSymbolTable.addressOf(name)))
            }
            is MiniJavaParser.ArrayAccessExprContext -> {
                val elementType = codeGenerationPhase.arrayAccessExpressionCodeGeneration.generateElementAddressCodeAndReturnElementType(left)

                codeGenerationPhase.visit(ctx.right)
                checkAndConvertAssigment(elementType)

                codeGenerationPhase.currentFunction.body.instructions.add(elementType.getStoreMemoryInstruction())

            }
            is MiniJavaParser.MemberExprContext -> {
                val type = codeGenerationPhase.memberAccessExpressionCodeGenerator.generateMemberExprAddressAndReturnResultingType(left)

                codeGenerationPhase.visit(ctx.right)
                checkAndConvertAssigment(type)

                codeGenerationPhase.currentFunction.body.instructions.add(type.getStoreMemoryInstruction())

            }
            else -> throw InvalidAssignmentException(left.start)
        }
    }
}