package dev.ssch.minijava.ast

sealed class Instruction {
    class i32_const (
        val value: Int
    ) : Instruction()

    class call (
        val address: Int
    ) : Instruction()

    class local_set (
        val address: Int
    ) : Instruction()

    class local_get (
        val address: Int
    ) : Instruction()

    class i32_add : Instruction()
    class i32_sub : Instruction()
    class i32_mul : Instruction()
    class i32_div_s : Instruction()
}

