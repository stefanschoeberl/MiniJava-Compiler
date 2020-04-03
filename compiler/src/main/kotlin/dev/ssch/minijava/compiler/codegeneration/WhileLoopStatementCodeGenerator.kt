package dev.ssch.minijava.compiler.codegeneration

import dev.ssch.minijava.compiler.CodeEmitter
import dev.ssch.minijava.compiler.DataType
import dev.ssch.minijava.compiler.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.wasm.ast.Instruction

class WhileLoopStatementCodeGenerator (
    private val codeEmitter: CodeEmitter,
    private val expressionCodeGenerator: ExpressionCodeGenerator,
    private val statementCodeGenerator: StatementCodeGenerator
) {

    fun generateExecution(ctx: MiniJavaParser.WhileLoopStmtContext) {
        codeEmitter.emitInstruction(Instruction.block)
        codeEmitter.emitInstruction(Instruction.loop)

        val conditionType = expressionCodeGenerator.generateEvaluation(ctx.condition)
        if (conditionType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(
                DataType.PrimitiveType.Boolean,
                conditionType,
                ctx.condition.getStart()
            )
        }
        codeEmitter.emitInstruction(Instruction.i32_eqz)
        codeEmitter.emitInstruction(Instruction.br_if(1))

        statementCodeGenerator.generateExecution(ctx.body)

        codeEmitter.emitInstruction(Instruction.br(0))

        codeEmitter.emitInstruction(Instruction.end)
        codeEmitter.emitInstruction(Instruction.end)
    }
}