package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.UndefinedVariableException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UndefinedVariableExceptionTest : CompilerTest() {

    @Test
    fun `use undefined variable in println`() {
        assertThatThrownBy {
        """
            println(a);
        """.runInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `use undefined variable in expression`() {
        assertThatThrownBy {
        """
            int a = 2 * b + 1;
        """.runInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `assign undefined variable`() {
        assertThatThrownBy {
        """
            a = 1;
        """.runInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }

    @Test
    fun `assign defined to undefined variable`() {
        assertThatThrownBy {
        """
            int b = 1;
            a = b;
        """.runInMainFunction()
        }.isInstanceOf(UndefinedVariableException::class.java)
    }
}