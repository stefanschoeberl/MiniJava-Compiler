package dev.ssch.minijava.compiler.expressions

import dev.ssch.minijava.compiler.exception.InvalidUnaryOperationException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MinusTest : CompilerTest() {

    @Test
    fun `minus expressions`() {
        val output = """
            Console.println(-1);
            Console.println(-123);
            Console.println(-(1+2));
            Console.println(-(100+23));
            Console.println(-(2147483640 + 0));
            Console.println(-1f);
            Console.println(-(1f+2f));
            Console.println(-(100f+3000f));
        """.compileAndRunInMainFunction()
        output.lines().matches(v(-1), v(-123), v(-3), v(-123), v(-2147483640), v(-1f, 0.0001f), v(-3f, 0.0001f), v(-3100f, 0.0001f), v(""))
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