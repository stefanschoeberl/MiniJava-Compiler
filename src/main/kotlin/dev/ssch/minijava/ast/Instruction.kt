package dev.ssch.minijava.ast

sealed class Instruction {
    data class i32_const (
        val value: Int
    ) : Instruction()

    data class call (
        val address: Int
    ) : Instruction()

    data class local_set (
        val address: Int
    ) : Instruction()

    data class local_get (
        val address: Int
    ) : Instruction()
}

