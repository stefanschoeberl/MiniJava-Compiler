package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EqualNotEqualTest : CompilerTest() {

    @Test
    fun `== two boolean values`() {
        val output = """
            Console.println(true == false);
            Console.println(false == true);
            Console.println(true == true);
            Console.println(false == false);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "false", "true", "true", "")
    }

    @Test
    fun `== two int values`() {
        val output = """
            Console.println(1 == 2);
            Console.println(1 == 1);
            Console.println(2 == 1);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "")
    }

    @Test
    fun `== two float values`() {
        val output = """
            Console.println(1f == 2f);
            Console.println(1f == 1f);
            Console.println(2f == 1f);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("false", "true", "false", "")
    }

    @Test
    fun `!= two boolean values`() {
        val output = """
            Console.println(true != false);
            Console.println(false != true);
            Console.println(true != true);
            Console.println(false != false);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "true", "false", "false", "")
    }

    @Test
    fun `!= two int values`() {
        val output = """
            Console.println(1 != 2);
            Console.println(1 != 1);
            Console.println(2 != 1);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "")
    }

    @Test
    fun `!= two float values`() {
        val output = """
            Console.println(1f != 2f);
            Console.println(1f != 1f);
            Console.println(2f != 1f);
        """.compileAndRunInMainFunction()
        assertThat(output.lines()).containsExactly("true", "false", "true", "")
    }

    @Test
    fun `== two variables 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a == b;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two variables 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b == a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two literals 1`() {
        assertThatThrownBy {
            """
                boolean a = 123 == false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two literals 2`() {
        assertThatThrownBy {
            """
                boolean a = false == 123;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two complex expressions 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a == (2 * 4);
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two complex expressions 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) == a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two variables 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a != b;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two variables 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b != a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two literals 1`() {
        assertThatThrownBy {
            """
                boolean a = 123 != false;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two literals 2`() {
        assertThatThrownBy {
            """
                boolean a = false != 123;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two complex expressions 1`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a != (2 * 4);
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two complex expressions 2`() {
        assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) != a;
            """.compileAndRunInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}