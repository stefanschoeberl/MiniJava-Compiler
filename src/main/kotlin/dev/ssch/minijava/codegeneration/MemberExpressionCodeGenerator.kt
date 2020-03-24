package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class MemberExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateEvaluation(ctx: MiniJavaParser.MemberExprContext): DataType {
        val fieldName = ctx.right.text
        return generateEvaluation(fieldName) {
            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.left)
        }
    }

    fun generateEvaluation(fieldName: String, objectAddressCode: () -> DataType?): DataType {
        val objType = objectAddressCode() as? DataType.ReferenceType ?: TODO()


        val field = codeGenerationPhase.classSymbolTable
            .getFieldSymbolTable(objType.name)
            .findFieldInfo(fieldName) ?: TODO()

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(field.getterAddress))
        return field.type
    }

    fun generateWrite(ctx: MiniJavaParser.MemberExprContext, right: MiniJavaParser.ExprContext) {
        val fieldName = ctx.right.text
        generateWrite(fieldName, right, ctx.right) {
            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.left)
        }
    }

    fun generateWrite(fieldName: String, right: MiniJavaParser.ExprContext, token: Token, objectAddressCode: () -> DataType?) {
        val objType = objectAddressCode() as? DataType.ReferenceType ?: TODO()

        val field = codeGenerationPhase.classSymbolTable
            .getFieldSymbolTable(objType.name)
            .findFieldInfo(fieldName) ?: TODO()

        val rightType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(right)
        codeGenerationPhase.variableAssignmentStatementCodeGenerator.checkAndConvertAssigment(field.type, rightType, token)
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.call(field.setterAddress))
    }
}