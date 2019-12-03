package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.RedefinedVariableException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RedefinedVariableExceptionTest : CompilerTest() {

    @Test
    fun `redefine variable`() {
        assertThatThrownBy {
        """
            int a;
            int a;
        """.runInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine variable with different type`() {
        assertThatThrownBy {
        """
            int a;
            boolean a;
        """.runInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine and assign variable`() {
        assertThatThrownBy {
        """
            int a;
            int a = 1;
        """.runInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine and assign variable with different type`() {
        assertThatThrownBy {
        """
            int a;
            boolean a = true;
        """.runInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine variable with same name as local variable`() {
        assertThatThrownBy {
        """
            void a(int x) {
                int x = 123;
            }
        """.compileAndRunMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }
}