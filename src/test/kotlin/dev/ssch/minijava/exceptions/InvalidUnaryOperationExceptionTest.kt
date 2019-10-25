package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.InvalidUnaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class InvalidUnaryOperationExceptionTest : CompilerTest() {

    @Test
    fun `minus variable`() {
        assertThatThrownBy {
        """
            boolean a = true;
            int c = -a;
        """.runInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }

    @Test
    fun `minus literal`() {
        assertThatThrownBy {
        """
            int a = -true;
        """.runInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }

    @Test
    fun `minus complex expression`() {
        assertThatThrownBy {
        """
            boolean a = true;
            int a = -(a);
        """.runInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }
}