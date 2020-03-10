package dev.ssch.minijava.codegeneration

import dev.ssch.minijava.CodeGenerationPhase
import dev.ssch.minijava.ast.Instruction
import dev.ssch.minijava.getDataType
import dev.ssch.minijava.grammar.MiniJavaParser
import dev.ssch.minijava.symboltable.LocalVariableSymbolTable
import dev.ssch.minijava.symboltable.MethodSymbolTable
import dev.ssch.minijava.toWebAssemblyType

class MethodCodeGenerator(private val codeGenerationPhase: CodeGenerationPhase): CodeGenerator(codeGenerationPhase) {

    fun generate(ctx: MiniJavaParser.MethodContext) {
        if (ctx.nativemodifier.isNotEmpty()) {
            return
        }

        // reset scope
        codeGenerationPhase.localsVariableSymbolTable = LocalVariableSymbolTable()

        val parameters = ctx.parameters.map { Pair(it.name.text, it.type.getDataType(codeGenerationPhase.classSymbolTable)!!) }
        val parameterTypes = parameters.map { it.second }
        codeGenerationPhase.currentFunction = codeGenerationPhase.functions[Pair(codeGenerationPhase.currentClass, MethodSymbolTable.MethodSignature(ctx.name.text, parameterTypes))]!!

        parameters.forEach {
            codeGenerationPhase.localsVariableSymbolTable.declareParameter(it.first, it.second)
        }

        ctx.statements.forEach {
            codeGenerationPhase.visit(it)
        }

        // "workaround" idea from https://github.com/WebAssembly/wabt/issues/1075
        if (codeGenerationPhase.methodSymbolTable.returnTypeOf(ctx.name.text, parameters.map { it.second }) != null) {
            codeGenerationPhase.currentFunction.body.instructions.add(Instruction.unreachable)
        }

        codeGenerationPhase.localsVariableSymbolTable.allLocalVariables.forEach {
            codeGenerationPhase.currentFunction.locals.add(it.toWebAssemblyType())
        }
    }
}