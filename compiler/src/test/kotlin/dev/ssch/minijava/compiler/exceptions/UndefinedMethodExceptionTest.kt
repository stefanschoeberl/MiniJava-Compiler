package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.UndefinedMethodException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UndefinedMethodExceptionTest : CompilerTest() {

    @Test
    fun `call undefined method`() {
        assertThatThrownBy {
        """
            undefinedMethod();
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedMethodException::class.java)
    }

    @Test
    fun `wrong parameter types`() {
        assertThatThrownBy {
        """
            static void a(int b) {}
            public static void main() {
                a(true);
            }
        """.compileAndRunInMainClass()
        }.isInstanceOf(UndefinedMethodException::class.java)
    }
}