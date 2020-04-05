package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.NotAStatementException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class NotAStatementExceptionTest : CompilerTest() {

    @Test
    fun `arithmetic expression`() {
        assertThatThrownBy {
        """
            1 + 2;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAStatementException::class.java)
    }

    @Test
    fun `String expression`() {
        assertThatThrownBy {
        """
            "test";
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAStatementException::class.java)
    }

    @Test
    fun `array creation`() {
        assertThatThrownBy {
        """
            new int[123];
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAStatementException::class.java)
    }

}