package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.grammar.MiniJavaBaseVisitor
import dev.ssch.minijava.grammar.MiniJavaParser

class StatementCodeGenerator(codeGenerationPhase: CodeGenerationPhase) {

    val whileLoopCodeGenerator = WhileLoopStatementCodeGenerator(codeGenerationPhase)
    val ifElseLoopCodeGenerator = IfElseStatementCodeGenerator(codeGenerationPhase)
    val variableDeclarationStatementCodeGenerator = VariableDeclarationStatementCodeGenerator(codeGenerationPhase)
    val variableAssignmentStatementCodeGenerator = codeGenerationPhase.variableAssignmentStatementCodeGenerator
    val basicStatementCodeGenerator = BasicStatementCodeGenerator(codeGenerationPhase)

    private val visitor = Visitor()

    fun generateExecution(ctx: MiniJavaParser.StatementContext) {
        visitor.visit(ctx)
    }

    fun generateExecution(ctx: MiniJavaParser.CompleteStatementContext) {
        visitor.visit(ctx)
    }

    fun generateExecution(ctx: MiniJavaParser.IncompleteIfStatementContext) {
        visitor.visit(ctx)
    }

    private inner class Visitor : MiniJavaBaseVisitor<Unit>() {
        override fun visitVardeclassignStmt(ctx: MiniJavaParser.VardeclassignStmtContext) {
            variableDeclarationStatementCodeGenerator.generate(ctx)
        }

        override fun visitVardeclStmt(ctx: MiniJavaParser.VardeclStmtContext) {
            variableDeclarationStatementCodeGenerator.generate(ctx)
        }

        override fun visitVarassignStmt(ctx: MiniJavaParser.VarassignStmtContext) {
            variableAssignmentStatementCodeGenerator.generateExecution(ctx)
        }

        override fun visitExpressionStmt(ctx: MiniJavaParser.ExpressionStmtContext) {
            basicStatementCodeGenerator.generateExecution(ctx)
        }

        override fun visitReturnStmt(ctx: MiniJavaParser.ReturnStmtContext) {
            basicStatementCodeGenerator.generateExecution(ctx)
        }

        override fun visitBlockStmt(ctx: MiniJavaParser.BlockStmtContext) {
            basicStatementCodeGenerator.generateExecution(ctx)
        }

        override fun visitCompleteIfElseStmt(ctx: MiniJavaParser.CompleteIfElseStmtContext) {
            ifElseLoopCodeGenerator.generateExecution(ctx)
        }

        override fun visitIncompleteIfStmt(ctx: MiniJavaParser.IncompleteIfStmtContext) {
            ifElseLoopCodeGenerator.generateExecution(ctx)
        }

        override fun visitIncompleteIfElseStmt(ctx: MiniJavaParser.IncompleteIfElseStmtContext) {
            ifElseLoopCodeGenerator.generateExecution(ctx)
        }

        override fun visitWhileLoopStmt(ctx: MiniJavaParser.WhileLoopStmtContext) {
            whileLoopCodeGenerator.generateExecution(ctx)
        }
    }
}