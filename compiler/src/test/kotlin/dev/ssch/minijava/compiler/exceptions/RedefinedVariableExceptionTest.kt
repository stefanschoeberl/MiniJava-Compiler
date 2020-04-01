package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.RedefinedVariableException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RedefinedVariableExceptionTest : CompilerTest() {

    @Test
    fun `redefine variable`() {
        assertThatThrownBy {
        """
            int a;
            int a;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine variable with different type`() {
        assertThatThrownBy {
        """
            int a;
            boolean a;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine and assign variable`() {
        assertThatThrownBy {
        """
            int a;
            int a = 1;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine and assign variable with different type`() {
        assertThatThrownBy {
        """
            int a;
            boolean a = true;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }

    @Test
    fun `redefine variable with same name as local variable`() {
        assertThatThrownBy {
        """
            static void a(int x) {
                int x = 123;
            }
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedVariableException::class.java)
    }
}