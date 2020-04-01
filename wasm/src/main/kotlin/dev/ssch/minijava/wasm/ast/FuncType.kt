package dev.ssch.minijava.wasm.ast

data class FuncType (
    val parameters: MutableList<ValueType>,
    val result: MutableList<ValueType> // currently restricted to length 1!
)