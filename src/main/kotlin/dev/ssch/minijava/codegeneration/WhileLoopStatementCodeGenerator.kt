package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.grammar.MiniJavaParser

class WhileLoopStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.WhileLoopStmtContext) {
        with(codeGenerationPhase.currentFunction.body.instructions) {
            add(dev.ssch.minijava.ast.Instruction.block)
            add(dev.ssch.minijava.ast.Instruction.loop)

            codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.condition)
            if (ctx.condition.staticType != dev.ssch.minijava.DataType.PrimitiveType.Boolean) {
                throw dev.ssch.minijava.exception.IncompatibleTypeException(
                    dev.ssch.minijava.DataType.PrimitiveType.Boolean,
                    ctx.condition.staticType,
                    ctx.condition.getStart()
                )
            }
            add(dev.ssch.minijava.ast.Instruction.i32_eqz)
            add(dev.ssch.minijava.ast.Instruction.br_if(1))

            codeGenerationPhase.visit(ctx.body)

            add(dev.ssch.minijava.ast.Instruction.br(0))

            add(dev.ssch.minijava.ast.Instruction.end)
            add(dev.ssch.minijava.ast.Instruction.end)
        }
    }
}