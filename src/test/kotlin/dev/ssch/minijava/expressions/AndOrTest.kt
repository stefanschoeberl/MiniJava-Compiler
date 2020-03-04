package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AndOrTest : CompilerTest() {

    @Test
    fun `&& two boolean values`() {
        val output = """
            Console.println(true && false);
            Console.println(false && true);
            Console.println(true && true);
            Console.println(false && false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "false", "true", "false", "")
    }

    @Test
    fun `|| two boolean values`() {
        val output = """
            Console.println(true || false);
            Console.println(false || true);
            Console.println(true || true);
            Console.println(false || false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "true", "true", "false", "")
    }

    @Test
    fun `&& two variables 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a && b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two variables 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b && a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two literals 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 && false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two literals 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = false && 123;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two complex expressions 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a && (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two complex expressions 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) && a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two variables 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a || b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two variables 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b || a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two literals 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 || false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two literals 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = false || 123;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two complex expressions 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a || (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two complex expressions 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) || a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `mixed boolean expressions`() {
        val output = """
            Console.println(true || (false && true));
            Console.println(true || false && true);
            Console.println((true || false) && true);
            Console.println((true || false) && (false || true));
            Console.println(true || false && false || true);
            Console.println(true && false || false && true);
            Console.println(true && false || true && false);
            Console.println(true && (false || true) && false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "true", "true", "true", "true", "false", "false", "false", "")
    }
}