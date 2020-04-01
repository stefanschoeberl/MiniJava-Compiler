package dev.ssch.minijava.wasm.ast

data class Import (
    val module: String,
    val name: String,
    val desc: ImportDesc
)