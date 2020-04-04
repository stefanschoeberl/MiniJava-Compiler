package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.symboltable.ClassSymbolTable
import dev.ssch.minijava.compiler.symboltable.StringLiteralSymbolTable
import dev.ssch.minijava.wasm.ast.Module

data class Bundle (
    val module: Module,
    val classSymbolTable: ClassSymbolTable,
    val stringLiteralSymbolTable: StringLiteralSymbolTable
)