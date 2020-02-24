package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidUnaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class MinusTest : CompilerTest() {

    @Test
    fun `minus expressions`() {
        val output = """
            println(-1);
            println(-(1+2));
            println(-1f);
        """.runInMainFunction()
        output.lines().matches(v(-1), v(-3), v(-1f, 0.0001f), v(""))
    }

    @Test
    fun `minus variable`() {
        Assertions.assertThatThrownBy {
            """
            boolean a = true;
            int c = -a;
        """.runInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }

    @Test
    fun `minus literal`() {
        Assertions.assertThatThrownBy {
            """
            int a = -true;
        """.runInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }

    @Test
    fun `minus complex expression`() {
        Assertions.assertThatThrownBy {
            """
            boolean a = true;
            int a = -(a);
        """.runInMainFunction()
        }.isInstanceOf(InvalidUnaryOperationException::class.java)
    }
}