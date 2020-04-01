package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.UndefinedVariableException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UndefinedVariableExceptionTest : CompilerTest() {

    @Test
    fun `use undefined variable in println`() {
        assertThatThrownBy {
        """
            println(a);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `use undefined variable in expression`() {
        assertThatThrownBy {
        """
            int a = 2 * b + 1;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `assign undefined variable`() {
        assertThatThrownBy {
        """
            a = 1;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `assign defined to undefined variable`() {
        assertThatThrownBy {
        """
            int b = 1;
            a = b;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }
}