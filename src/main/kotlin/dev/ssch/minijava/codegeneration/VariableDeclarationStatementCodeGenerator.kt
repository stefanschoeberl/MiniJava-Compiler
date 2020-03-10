package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.DataType
import dev.ssch.minijava.assignTypeTo
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.exception.IncompatibleAssignmentException
import dev.ssch.minijava.exception.RedefinedVariableException
import dev.ssch.minijava.exception.UnknownTypeException
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import org.antlr.v4.runtime.Token

class VariableDeclarationStatementCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generate(ctx: MiniJavaParser.VardeclStmtContext) {
        declareVariable(ctx.name, ctx.type)
    }

    fun generate(ctx: MiniJavaParser.VardeclassignStmtContext) {
        codeGenerationPhase.expressionCodeGenerator.generateEvaluation(ctx.expr())

        val (name, type) = declareVariable(ctx.name, ctx.type)

        val conversionCode = ctx.expr().staticType!!.assignTypeTo(type)
            ?: throw IncompatibleAssignmentException(type, ctx.expr().staticType, ctx.name)

        codeGenerationPhase.currentFunction.body.instructions.addAll(conversionCode)

        codeGenerationPhase.currentFunction.body.instructions.add(Instruction.local_set(codeGenerationPhase.localsVariableSymbolTable.addressOf(name)))
    }

    private fun declareVariable(nameToken: Token, typeContext: MiniJavaParser.TypeDefinitionContext): Pair<String, DataType> {
        val name = nameToken.text
        val type = typeContext.getDataType(codeGenerationPhase.classSymbolTable)
            ?: throw UnknownTypeException(typeContext.text, typeContext.start)
        if (codeGenerationPhase.localsVariableSymbolTable.isDeclared(name)) {
            throw RedefinedVariableException(name, nameToken)
        }
        codeGenerationPhase.localsVariableSymbolTable.declareVariable(name, type)
        return Pair(name, type)
    }
}