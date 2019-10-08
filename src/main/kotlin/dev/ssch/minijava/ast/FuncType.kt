package dev.ssch.minijava.ast

data class FuncType (
    val parameters: MutableList<ValueType>,
    val result: MutableList<ValueType> // currently restricted to length 1!
)