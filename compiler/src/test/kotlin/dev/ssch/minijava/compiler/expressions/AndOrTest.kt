package dev.ssch.minijava.compiler.expressions

import dev.ssch.minijava.compiler.exception.InvalidBinaryOperationException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AndOrTest : CompilerTest() {

    @Test
    fun `&& two boolean values`() {
        val output = """
            Console.println(true && false);
            Console.println(false && true);
            Console.println(true && true);
            Console.println(false && false);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "false", "true", "false", "")
    }

    @Test
    fun `|| two boolean values`() {
        val output = """
            Console.println(true || false);
            Console.println(false || true);
            Console.println(true || true);
            Console.println(false || false);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "true", "true", "false", "")
    }

    @Test
    fun `&& two variables 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a && b;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two variables 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b && a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two literals 1`() {
        assertThatThrownBy {
            """
                boolean a = 123 && false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two literals 2`() {
        assertThatThrownBy {
            """
                boolean a = false && 123;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two complex expressions 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a && (2 * 4);
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `&& two complex expressions 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) && a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two variables 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a || b;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two variables 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b || a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two literals 1`() {
        assertThatThrownBy {
            """
                boolean a = 123 || false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two literals 2`() {
        assertThatThrownBy {
            """
                boolean a = false || 123;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two complex expressions 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a || (2 * 4);
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `|| two complex expressions 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) || a;
            """.compileAndRunInMainFunction()
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
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "true", "true", "true", "true", "false", "false", "false", "")
    }
}