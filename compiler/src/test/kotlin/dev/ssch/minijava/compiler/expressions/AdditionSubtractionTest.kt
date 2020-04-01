package dev.ssch.minijava.compiler.expressions

import dev.ssch.minijava.compiler.exception.InvalidBinaryOperationException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AdditionSubtractionTest : CompilerTest() {

    @Test
    fun `add two int values`() {
        val output = """
            Console.println(1 + 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("3", "")
    }

    @Test
    fun `add two float values`() {
        val output = """
            Console.println(1f + 2f);
        """.compileAndRunInMainFunction()
        output.lines().matches(v(3f, 0.0001f), v(""))
    }

    @Test
    fun `add two negative values`() {
        val output = """
            Console.println(-1 + -2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("-3", "")
    }

    @Test
    fun `add two variables`() {
        val output = """
            int a = 1;
            int b = 2;
            Console.println(a + b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("3", "")
    }

    @Test
    fun `add two negative variables`() {
        val output = """
            int a = 1;
            int b = 2;
            Console.println(-a + -b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("-3", "")
    }

    @Test
    fun `nested addition`() {
        val output = """
            Console.println((1 + 2) + 3);
            Console.println(1 + 2 + 3);
            Console.println(1 + (2 + 3));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("6", "6", "6", "")
    }

    @Test
    fun `subtract two int values`() {
        val output = """
            Console.println(1 - 2);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `subtract two float values`() {
        val output = """
            Console.println(1f - 2f);
        """.compileAndRunInMainFunction()
        output.lines().matches(v(-1f, 0.0001f), v(""))
    }

    @Test
    fun `subtract two variables`() {
        val output = """
            int a = 1;
            int b = 2;
            Console.println(a - b);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("-1", "")
    }

    @Test
    fun `nested subtraction`() {
        val output = """
            Console.println((1 - 2) - 3);
            Console.println(1 - 2 - 3);
            Console.println(1 - (2 - 3));
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("-4", "-4", "2", "")
    }

    @Test
    fun `add two boolean variables`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = true;
                int c = a + b;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `add two boolean literals`() {
        assertThatThrownBy {
            """
                int a = true + false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `add two complex expressions`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int a = a + (2 * 4);
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two boolean variables`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = true;
                int c = a - b;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two boolean literals`() {
        assertThatThrownBy {
            """
                int a = true - false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two complex expressions`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int a = a - (2 * 4);
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}