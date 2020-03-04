package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class RelationalTest : CompilerTest() {

    @Test
    fun `lt two int values`() {
        val output = """
            Console.println(1 < 2);
            Console.println(2 < 1);
            Console.println(-1 < 15);
            Console.println(15 < -1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

    @Test
    fun `lt two float values`() {
        val output = """
            Console.println(1f < 2f);
            Console.println(2f < 1f);
            Console.println(-1f < 15f);
            Console.println(15f < -1f);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
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
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "true", "true", "")
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
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "true", "true", "")
    }

    @Test
    fun `gt two int values`() {
        val output = """
            Console.println(1 > 2);
            Console.println(2 > 1);
            Console.println(-1 > 15);
            Console.println(15 > -1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "true", "")
    }

    @Test
    fun `gt two float values`() {
        val output = """
            Console.println(1f > 2f);
            Console.println(2f > 1f);
            Console.println(-1f > 15f);
            Console.println(15f > -1f);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "true", "")
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
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "true", "true", "true", "")
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
            Console.println(1 < 2 && 2 < 3);
            Console.println(1 > 2 && 2 > 3);
            Console.println(1 <= 2 && 2 <= 3);
            Console.println(1 >= 2 && 2 >= 3);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "false", "")
    }

}