package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.IncompatibleAssignmentException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class IncompatibleAssignmentExceptionTest : CompilerTest() {

    @Test
    fun `declare and assign bool to int`() {
        assertThatThrownBy {
        """
            boolean a = 123;
        """.runInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }

    @Test
    fun `declare and assign int to bool`() {
        assertThatThrownBy {
            """
            int a = false;
        """.runInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }

    @Test
    fun `assign bool to int`() {
        assertThatThrownBy {
            """
            boolean a;
            a = 123;
        """.runInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }

    @Test
    fun `assign int to bool`() {
        assertThatThrownBy {
            """
            int a;
            a = false;
        """.runInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }
}