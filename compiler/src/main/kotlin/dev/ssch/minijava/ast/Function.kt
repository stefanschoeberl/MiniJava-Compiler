package dev.ssch.minijava.ast

data class Function (
    val typeidx: Int,
    val locals: MutableList<ValueType>,
    val body: Expr
)