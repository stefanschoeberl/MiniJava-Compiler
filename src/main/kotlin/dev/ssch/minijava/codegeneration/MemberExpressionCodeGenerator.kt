package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.getLoadMemoryInstruction
import dev.ssch.minijava.grammar.MiniJavaParser

class MemberExpressionCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateMemberExprAddressAndReturnResultingType(ctx: MiniJavaParser.MemberExprContext): DataType {
        return generateMemberExprAddressAndReturnResultingType(ctx.right.text) {
            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())
        }
    }

    fun generateMemberExprAddressAndReturnResultingType(fieldName: String, objectAddressCode: () -> DataType?): DataType {
        val type = objectAddressCode() as? DataType.ReferenceType ?: TODO()
        val fieldSymbolTable = codeGenerationPhase.classSymbolTable.getFieldSymbolTable(type.name)
        val fieldInfo = fieldSymbolTable.findFieldInfo(fieldName) ?: TODO()

        // add offset
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_const(fieldInfo.offset))
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.i32_add)

        return fieldInfo.type
    }

    fun generateEvaluation(ctx: MiniJavaParser.MemberExprContext): DataType {
        val type = generateMemberExprAddressAndReturnResultingType(ctx)
        codeGenerationPhase.currentFunction.body.instructions.add(type.getLoadMemoryInstruction())
        return type
    }

    fun generateEvaluation(fieldName: String, objectAddressCode: () -> DataType?): DataType {
        val type = generateMemberExprAddressAndReturnResultingType(fieldName, objectAddressCode)
        codeGenerationPhase.currentFunction.body.instructions.add(type.getLoadMemoryInstruction())
        return type
    }
}