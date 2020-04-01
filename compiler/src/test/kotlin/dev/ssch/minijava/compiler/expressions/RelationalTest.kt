package dev.ssch.minijava.compiler.expressions

import dev.ssch.minijava.compiler.exception.InvalidBinaryOperationException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RelationalTest : CompilerTest() {

    @Test
    fun `lt two int values`() {
        val output = """
            Console.println(1 < 2);
            Console.println(2 < 1);
            Console.println(-1 < 15);
            Console.println(15 < -1);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

    @Test
    fun `lt two float values`() {
        val output = """
            Console.println(1f < 2f);
            Console.println(2f < 1f);
            Console.println(-1f < 15f);
            Console.println(15f < -1f);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

    @Test
    fun `lte two int values`() {
        val output = """
            Console.println(1 <= 2);
            Console.println(2 <= 1);
            Console.println(-1 <= 15);
            Console.println(15 <= -1);
            Console.println(1 <= 1);
            Console.println(-10 <= -10);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "false", "true", "true", "")
    }

    @Test
    fun `lte two float values`() {
        val output = """
            Console.println(1f <= 2f);
            Console.println(2f <= 1f);
            Console.println(-1f <= 15f);
            Console.println(15f <= -1f);
            Console.println(1f <= 1f);
            Console.println(-10f <= -10f);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "false", "true", "true", "")
    }

    @Test
    fun `gt two int values`() {
        val output = """
            Console.println(1 > 2);
            Console.println(2 > 1);
            Console.println(-1 > 15);
            Console.println(15 > -1);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "true", "")
    }

    @Test
    fun `gt two float values`() {
        val output = """
            Console.println(1f > 2f);
            Console.println(2f > 1f);
            Console.println(-1f > 15f);
            Console.println(15f > -1f);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "true", "")
    }

    @Test
    fun `gte two int values`() {
        val output = """
            Console.println(1 >= 2);
            Console.println(2 >= 1);
            Console.println(-1 >= 15);
            Console.println(15 >= -1);
            Console.println(1 >= 1);
            Console.println(-10 >= -10);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "true", "true", "true", "")
    }

    @Test
    fun `gte two float values`() {
        val output = """
            Console.println(1f >= 2f);
            Console.println(2f >= 1f);
            Console.println(-1f >= 15f);
            Console.println(15f >= -1f);
            Console.println(1f >= 1f);
            Console.println(-10f >= -10f);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "true", "true", "true", "")
    }

    @Test
    fun `lt two incompatible literals`() {
        assertThatThrownBy {
            """
                boolean a = 123 < false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `lte two incompatible literals`() {
        assertThatThrownBy {
            """
                boolean a = 123 <= false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `gt two incompatible literals`() {
        assertThatThrownBy {
            """
                boolean a = 123 > false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `gte two incompatible literals`() {
        assertThatThrownBy {
            """
                boolean a = 123 >= false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `complex expressions`() {
        val output = """
            Console.println(1 < 2 && 2 < 3);
            Console.println(1 > 2 && 2 > 3);
            Console.println(1 <= 2 && 2 <= 3);
            Console.println(1 >= 2 && 2 >= 3);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

}