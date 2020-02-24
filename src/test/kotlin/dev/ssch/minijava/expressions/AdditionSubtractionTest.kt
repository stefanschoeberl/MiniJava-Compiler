package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AdditionSubtractionTest : CompilerTest() {

    @Test
    fun `add two int values`() {
        val output = """
            println(1 + 2);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("3", "")
    }

    @Test
    fun `add two float values`() {
        val output = """
            println(1f + 2f);
        """.runInMainFunction()
        output.lines().matches(v(3f, 0.0001f), v(""))
    }

    @Test
    fun `add two negative values`() {
        val output = """
            println(-1 + -2);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("-3", "")
    }

    @Test
    fun `add two variables`() {
        val output = """
            int a = 1;
            int b = 2;
            println(a + b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("3", "")
    }

    @Test
    fun `add two negative variables`() {
        val output = """
            int a = 1;
            int b = 2;
            println(-a + -b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("-3", "")
    }

    @Test
    fun `nested addition`() {
        val output = """
            println((1 + 2) + 3);
            println(1 + 2 + 3);
            println(1 + (2 + 3));
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("6", "6", "6", "")
    }

    @Test
    fun `subtract two int values`() {
        val output = """
            println(1 - 2);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `subtract two float values`() {
        val output = """
            println(1f - 2f);
        """.runInMainFunction()
        output.lines().matches(v(-1f, 0.0001f), v(""))
    }

    @Test
    fun `subtract two variables`() {
        val output = """
            int a = 1;
            int b = 2;
            println(a - b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `nested subtraction`() {
        val output = """
            println((1 - 2) - 3);
            println(1 - 2 - 3);
            println(1 - (2 - 3));
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("-4", "-4", "2", "")
    }

    @Test
    fun `add two boolean variables`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = true;
                int c = a + b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `add two boolean literals`() {
        Assertions.assertThatThrownBy {
            """
                int a = true + false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `add two complex expressions`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int a = a + (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two boolean variables`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = true;
                int c = a - b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two boolean literals`() {
        Assertions.assertThatThrownBy {
            """
                int a = true - false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two complex expressions`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int a = a - (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}