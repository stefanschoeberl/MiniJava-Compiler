package dev.ssch.minijava.ast

data class Import (
    val module: String,
    val name: String,
    val desc: ImportDesc
)