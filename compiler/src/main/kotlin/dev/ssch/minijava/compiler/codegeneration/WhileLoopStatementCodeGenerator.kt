package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeGenerationPhase
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class WhileLoopStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.WhileLoopStmtContext) {
        codeGenerationPhase.emitInstruction(Instruction.block)
        codeGenerationPhase.emitInstruction(Instruction.loop)

        val conditionType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.condition)
        if (conditionType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(
                DataType.PrimitiveType.Boolean,
                conditionType,
                ctx.condition.getStart()
            )
        }
        codeGenerationPhase.emitInstruction(Instruction.i32_eqz)
        codeGenerationPhase.emitInstruction(Instruction.br_if(1))

        codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.body)

        codeGenerationPhase.emitInstruction(Instruction.br(0))

        codeGenerationPhase.emitInstruction(Instruction.end)
        codeGenerationPhase.emitInstruction(Instruction.end)
    }
}