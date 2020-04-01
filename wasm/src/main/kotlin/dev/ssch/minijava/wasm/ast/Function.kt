package dev.ssch.minijava.wasm.ast

data class Function (
    val typeidx: Int,
    val locals: MutableList<ValueType>,
    val body: Expr
)