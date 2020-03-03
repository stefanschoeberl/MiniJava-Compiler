package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.InconvertibleTypeException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class InconvertibleTypeExceptionTest : CompilerTest() {

    @Test
    fun `cast bool to int`() {
        assertThatThrownBy {
        """
            int a = (int) true;
        """.runInMainFunction()
        }.isInstanceOf(InconvertibleTypeException::class.java)
    }

    @Test
    fun `cast bool to float`() {
        assertThatThrownBy {
        """
            float a = (float) true;
        """.runInMainFunction()
        }.isInstanceOf(InconvertibleTypeException::class.java)
    }

    @Test
    fun `cast void method to int`() {
        assertThatThrownBy {
        """
            static void test() {}
            public static void main() {
                int a = (int) test();
            }
        """.compileAndRunMainFunctionInMainClass()
        }.isInstanceOf(InconvertibleTypeException::class.java)
    }
}