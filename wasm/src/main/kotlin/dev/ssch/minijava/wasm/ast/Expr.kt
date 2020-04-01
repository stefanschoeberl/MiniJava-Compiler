package dev.ssch.minijava.wasm.ast

data class Expr (
    val instructions: MutableList<Instruction>
)