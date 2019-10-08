package dev.ssch.minijava.ast

sealed class Instruction {
    data class I32_const (
        val value: Int
    ) : Instruction()

    data class Call (
        val address: Int
    ) : Instruction()
}

