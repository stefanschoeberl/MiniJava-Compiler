package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class WhileLoopStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.WhileLoopStmtContext) {
        with(codeGenerationPhase.currentFunction.body.instructions) {
            add(Instruction.block)
            add(Instruction.loop)

            val conditionType = codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.condition)
            if (conditionType != dev.ssch.minijava.DataType.PrimitiveType.Boolean) {
                throw dev.ssch.minijava.exception.IncompatibleTypeException(
                    dev.ssch.minijava.DataType.PrimitiveType.Boolean,
                    conditionType,
                    ctx.condition.getStart()
                )
            }
            add(Instruction.i32_eqz)
            add(Instruction.br_if(1))

            codeGenerationPhase.statementCodeGenerator.generateExecution(ctx.body)

            add(Instruction.br(0))

            add(Instruction.end)
            add(Instruction.end)
        }
    }
}