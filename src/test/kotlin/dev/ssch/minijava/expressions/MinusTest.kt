package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidUnaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MinusTest : CompilerTest() {

    @Test
    fun `minus expressions`() {
        val output = """
            Console.println(-1);
            Console.println(-(1+2));
            Console.println(-1f);
        """.compileAndRunInMainFunction()
        output.lines().matches(v(-1), v(-3), v(-1f, 0.0001f), v(""))
    }

    @Test
    fun `minus variable`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int c = -a;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }

    @Test
    fun `minus literal`() {
        assertThatThrownBy {
            """
            int a = -true;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }

    @Test
    fun `minus complex expression`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int a = -(a);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }
}