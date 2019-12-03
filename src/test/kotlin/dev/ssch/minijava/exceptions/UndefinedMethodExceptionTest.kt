package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.UndefinedMethodException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UndefinedMethodExceptionTest : CompilerTest() {

    @Test
    fun `call undefined method`() {
        assertThatThrownBy {
        """
            undefinedMethod();
        """.runInMainFunction()
        }.isInstanceOf(UndefinedMethodException::class.java)
    }

    @Test
    fun `wrong parameter types`() {
        assertThatThrownBy {
        """
            void a(int b) {}
            public void main() {
                a(true);
            }
        """.compileAndRunMainFunction()
        }.isInstanceOf(UndefinedMethodException::class.java)
    }
}