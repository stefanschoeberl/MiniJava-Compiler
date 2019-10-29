package dev.ssch.minijava.expressions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class EqualNotEqualTest : CompilerTest() {

    @Test
    fun `== two boolean values`() {
        val output = """
            println(true == false);
            println(false == true);
            println(true == true);
            println(false == false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "false", "true", "true", "")
    }

    @Test
    fun `== two arithmetic values`() {
        val output = """
            println(1 == 2);
            println(1 == 1);
            println(2 == 1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("false", "true", "false", "")
    }

    @Test
    fun `!= two boolean values`() {
        val output = """
            println(true != false);
            println(false != true);
            println(true != true);
            println(false != false);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "true", "false", "false", "")
    }

    @Test
    fun `!= two arithmetic values`() {
        val output = """
            println(1 != 2);
            println(1 != 1);
            println(2 != 1);
        """.runInMainFunction()
        Assertions.assertThat(output.lines()).containsExactly("true", "false", "true", "")
    }

    @Test
    fun `== two variables 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a == b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two variables 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b == a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two literals 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 == false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two literals 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = false == 123;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two complex expressions 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a == (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two complex expressions 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) == a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two variables 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = a != b;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two variables 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                int b = 123;
                boolean c = b != a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two literals 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = 123 != false;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two literals 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = false != 123;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two complex expressions 1`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = a != (2 * 4);
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two complex expressions 2`() {
        Assertions.assertThatThrownBy {
            """
                boolean a = true;
                boolean b = (2 * 4) != a;
            """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}