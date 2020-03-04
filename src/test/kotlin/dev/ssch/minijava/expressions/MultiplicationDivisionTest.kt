package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class MultiplicationDivisionTest : CompilerTest() {

    @Test
    fun `multiply two int values`() {
        val output = """
            Console.println(2 * 3);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("6", "")
    }

    @Test
    fun `multiply two float values`() {
        val output = """
            Console.println(2f * 3f);
        """.runInMainFunction()
        output.lines().matches(v(6f, 0.0001f), v(""))
    }

    @Test
    fun `multiply two negative values`() {
        val output = """
            Console.println(-2 * -3);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("6", "")
    }

    @Test
    fun `multiply two variables`() {
        val output = """
            int a = 2;
            int b = 3;
            Console.println(a * b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("6", "")
    }

    @Test
    fun `multiply two negative variables`() {
        val output = """
            int a = 2;
            int b = 3;
            Console.println(-a * -b);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("6", "")
    }

    @Test
    fun `nested multiplication`() {
        val output = """
            Console.println((2 * 3) * 4);
            Console.println(2 * 3 * 4);
            Console.println(2 * (3 * 4));
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("24", "24", "24", "")
    }

    @Test
    fun `divide two int values`() {
        val output = """
            Console.println(1 / 2);
            Console.println(8 / 3);
            Console.println(6 / 3);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("0", "2", "2", "")
    }

    @Test
    fun `divide two float values`() {
        val output = """
            Console.println(1f / 2f);
            Console.println(8f / 3f);
            Console.println(6f / 3f);
        """.runInMainFunction()
        output.lines().matches(
            v(1/2f, 0.0001f),
            v(8/3f, 0.0001f),
            v(6/3f, 0.0001f),
            v(""))
    }

    @Test
    fun `divide two negative values`() {
        val output = """
            Console.println(-1 / 2);
            Console.println(8 / -3);
            Console.println(-6 / -3);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("0", "-2", "2", "")
    }

    @Test
    fun `divide two variables`() {
        val output = """
            int one = 1;
            int two = 2;
            int three = 3;
            int six = 6;
            int eight = 8;
            Console.println(one / two);
            Console.println(eight / three);
            Console.println(six / three);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("0", "2", "2", "")
    }

    @Test
    fun `divide two negative variables`() {
        val output = """
            int one = 1;
            int two = 2;
            int three = 3;
            int six = 6;
            int eight = 8;
            Console.println(-one / two);
            Console.println(eight / -three);
            Console.println(-six / -three);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("0", "-2", "2", "")
    }

    @Test
    fun `nested division`() {
        val output = """
            Console.println((8 / 3) / 2);
            Console.println(8 / 3 / 2);
            Console.println(8 / (3 / 2));
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("1", "1", "8", "")
    }

    @Test
    fun `multiply two boolean variables`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = true;
                int c = a * b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `multiply two boolean literals`() {
        Assertions.assertThatThrownBy {
            """
                int a = true * false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `multiply two complex expressions`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int a = a * (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `divide two boolean variables`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = true;
                int c = a / b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `divide two boolean literals`() {
        Assertions.assertThatThrownBy {
            """
                int a = true / false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `divide two complex expressions`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int a = a / (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}