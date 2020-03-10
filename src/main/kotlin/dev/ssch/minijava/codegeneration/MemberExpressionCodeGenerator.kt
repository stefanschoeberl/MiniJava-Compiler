package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.getLoadMemoryInstruction
import dev.ssch.minijava.grammar.MiniJavaParser

class MemberExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateMemberExprAddressAndReturnResultingType(ctx: MiniJavaParser.MemberExprContext): DataType {
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())
        val type = ctx.expr().staticType as? DataType.ReferenceType ?: TODO()
        val fieldName = ctx.right.text
        val fieldSymbolTable = codeGenerationPhase.classSymbolTable.getFieldSymbolTable(type.name)
        val fieldInfo = fieldSymbolTable.findFieldInfo(fieldName) ?: TODO()

        // add offset
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(fieldInfo.offset))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        return fieldInfo.type
    }

    fun generateEvaluation(ctx: MiniJavaParser.MemberExprContext) {
        val type = generateMemberExprAddressAndReturnResultingType(ctx)
        codeGenerationPhase.currentFunction.body.instructions.add(type.getLoadMemoryInstruction())
        ctx.staticType = type
    }
}