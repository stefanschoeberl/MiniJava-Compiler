package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.IncompatibleTypeException
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.tree.ParseTree

class IfElseStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generateExecution(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
        generateIfElse(ctx.condition, ctx.thenbranch, ctx.elsebranch)
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfStmtContext) {
        generateIfElse(ctx.condition, ctx.thenbranch)
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
        generateIfElse(ctx.condition, ctx.thenbranch, ctx.thenbranch)
    }

    private fun generateIfElse(condition: MiniJavaParser.ExprContext, thenbranch: ParseTree, elsebranch: ParseTree? = null) {
        codeGenerationPhase.visit(condition)
        if (condition.staticType != DataType.PrimitiveType.Boolean) {
            throw IncompatibleTypeException(DataType.PrimitiveType.Boolean, condition.staticType, condition.getStart())
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction._if)
        codeGenerationPhase.visit(thenbranch)
        if (elsebranch != null) {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction._else)
            codeGenerationPhase.visit(elsebranch)
        }
        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.end)
    }
}