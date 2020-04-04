package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.codegeneration.*
import dev.ssch.minijava.wasm.WebAssemblyAssembler
import dev.ssch.minijava.wasm.WebAssemblyModuleGenerator

class CompilationContext {

    val webAssemblyAssembler = WebAssemblyAssembler()
    val webAssemblyModuleGenerator = WebAssemblyModuleGenerator()
    val externalFunctionNameProvider = ExternalFunctionNameProvider()

    val declarationPhase = DeclarationPhase()
    val bundleGenerator = BundleGenerator(webAssemblyModuleGenerator, webAssemblyAssembler, externalFunctionNameProvider)

    val builtinFunctions = BuiltinFunctions()

    val codeEmitter = CodeEmitter()
    val statementCodeGenerator = StatementCodeGenerator()
    val expressionCodeGenerator = ExpressionCodeGenerator()

    val methodCodeGenerator = MethodCodeGenerator(codeEmitter, statementCodeGenerator)

    val classCodeGenerator = ClassCodeGenerator(codeEmitter, methodCodeGenerator)
    val codeGenerationPhase = ModuleCodeGenerator(
        classCodeGenerator,
        codeEmitter,
        builtinFunctions,
        externalFunctionNameProvider
    )
    val compiler = Compiler(declarationPhase, codeGenerationPhase)

    val operatorTable = OperatorTable(builtinFunctions)

    val arrayAccessExpressionCodeGeneration = ArrayAccessExpressionCodeGenerator(codeEmitter, expressionCodeGenerator, builtinFunctions)
    val arrayCreationExpressionCodeGenerator = ArrayCreationExpressionCodeGenerator(codeEmitter, expressionCodeGenerator, builtinFunctions)
    val binaryExpressionCodeGenerator = BinaryExpressionCodeGenerator(codeEmitter, expressionCodeGenerator, operatorTable)
    val classInstanceCreationExpressionCodeGenerator = ClassInstanceCreationExpressionCodeGenerator(codeEmitter, expressionCodeGenerator)
    val memberExpressionCodeGenerator = MemberExpressionCodeGenerator(codeEmitter, expressionCodeGenerator)
    val basicExpressionCodeGenerator = BasicExpressionCodeGenerator(codeEmitter, expressionCodeGenerator, memberExpressionCodeGenerator, operatorTable)
    val callExpressionCodeGenerator = CallExpressionCodeGeneration(codeEmitter, expressionCodeGenerator, basicExpressionCodeGenerator)

    val whileLoopStatementCodeGenerator = WhileLoopStatementCodeGenerator(codeEmitter, expressionCodeGenerator, statementCodeGenerator)
    val basicStatementCodeGenerator = BasicStatementCodeGenerator(codeEmitter, expressionCodeGenerator, statementCodeGenerator)
    val ifElseStatementCodeGenerator = IfElseStatementCodeGenerator(codeEmitter, statementCodeGenerator, expressionCodeGenerator)
    val variableDeclarationStatementCodeGenerator = VariableDeclarationStatementCodeGenerator(codeEmitter, expressionCodeGenerator)
    val variableAssignmentStatementCodeGenerator = VariableAssignmentStatementCodeGenerator(codeEmitter, expressionCodeGenerator, arrayAccessExpressionCodeGeneration, builtinFunctions)

    init {
        expressionCodeGenerator.init(
            basicExpressionCodeGenerator,
            binaryExpressionCodeGenerator,
            arrayCreationExpressionCodeGenerator,
            arrayAccessExpressionCodeGeneration,
            callExpressionCodeGenerator,
            classInstanceCreationExpressionCodeGenerator,
            memberExpressionCodeGenerator
        )
        statementCodeGenerator.init(
            whileLoopStatementCodeGenerator,
            ifElseStatementCodeGenerator,
            variableDeclarationStatementCodeGenerator,
            variableAssignmentStatementCodeGenerator,
            basicStatementCodeGenerator
        )
    }
}