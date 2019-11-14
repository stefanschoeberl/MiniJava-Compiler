package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class RelationalTest : CompilerTest() {

    @Test
    fun `lt two values`() {
        val output = """
            println(1 < 2);
            println(2 < 1);
            println(-1 < 15);
            println(15 < -1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

    @Test
    fun `lte two values`() {
        val output = """
            println(1 <= 2);
            println(2 <= 1);
            println(-1 <= 15);
            println(15 <= -1);
            println(1 <= 1);
            println(-10 <= -10);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "true", "true", "")
    }

    @Test
    fun `gt two values`() {
        val output = """
            println(1 > 2);
            println(2 > 1);
            println(-1 > 15);
            println(15 > -1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "true", "")
    }

    @Test
    fun `gte two values`() {
        val output = """
            println(1 >= 2);
            println(2 >= 1);
            println(-1 >= 15);
            println(15 >= -1);
            println(1 >= 1);
            println(-10 >= -10);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "true", "true", "true", "")
    }

    @Test
    fun `lt two incompatible literals`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 < false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `lte two incompatible literals`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 <= false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `gt two incompatible literals`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 > false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `gte two incompatible literals`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 >= false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `complex expressions`() {
        val output = """
            println(1 < 2 && 2 < 3);
            println(1 > 2 && 2 > 3);
            println(1 <= 2 && 2 <= 3);
            println(1 >= 2 && 2 >= 3);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

}