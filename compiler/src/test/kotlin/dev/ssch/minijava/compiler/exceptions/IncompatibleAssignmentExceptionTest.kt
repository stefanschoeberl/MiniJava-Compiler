package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.IncompatibleAssignmentException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class IncompatibleAssignmentExceptionTest : CompilerTest() {

    @Test
    fun `declare and assign bool to int`() {
        assertThatThrownBy {
        """
            boolean a = 123;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }

    @Test
    fun `declare and assign int to bool`() {
        assertThatThrownBy {
        """
            int a = false;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }

    @Test
    fun `assign bool to int`() {
        assertThatThrownBy {
        """
            boolean a;
            a = 123;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }

    @Test
    fun `assign int to bool`() {
        assertThatThrownBy {
        """
            int a;
            a = false;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(IncompatibleAssignmentException::class.java)
    }
}