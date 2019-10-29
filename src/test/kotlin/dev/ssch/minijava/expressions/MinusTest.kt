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
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("-1", "-3", "")
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