package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.InvalidBinaryOperationException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class InvalidBinaryOperationExceptionTest : CompilerTest() {

    @Test
    fun `add two variables`() {
        assertThatThrownBy {
        """
            boolean a = true;
            boolean b = true;
            int c = a + b;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `add two literals`() {
        assertThatThrownBy {
        """
            int a = true + false;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `add two complex expressions`() {
        assertThatThrownBy {
        """
            boolean a = true;
            int a = a + (2 * 4);
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    // -----

    @Test
    fun `subtract two variables`() {
        assertThatThrownBy {
        """
            boolean a = true;
            boolean b = true;
            int c = a - b;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two literals`() {
        assertThatThrownBy {
        """
            int a = true - false;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `subtract two complex expressions`() {
        assertThatThrownBy {
        """
            boolean a = true;
            int a = a - (2 * 4);
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    // -----

    @Test
    fun `multiply two variables`() {
        assertThatThrownBy {
            """
            boolean a = true;
            boolean b = true;
            int c = a * b;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `multiply two literals`() {
        assertThatThrownBy {
            """
            int a = true * false;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `multiply two complex expressions`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int a = a * (2 * 4);
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    // -----

    @Test
    fun `divide two variables`() {
        assertThatThrownBy {
            """
            boolean a = true;
            boolean b = true;
            int c = a / b;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `divide two literals`() {
        assertThatThrownBy {
            """
            int a = true / false;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `divide two complex expressions`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int a = a / (2 * 4);
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    // -----

    @Test
    fun `== two variables 1`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int b = 123;
            boolean c = a == b;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two variables 2`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int b = 123;
            boolean c = b == a;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two literals 1`() {
        assertThatThrownBy {
            """
            boolean a = 123 == false;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two literals 2`() {
        assertThatThrownBy {
            """
            boolean a = false == 123;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two complex expressions 1`() {
        assertThatThrownBy {
            """
            boolean a = true;
            boolean b = a == (2 * 4);
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `== two complex expressions 2`() {
        assertThatThrownBy {
            """
            boolean a = true;
            boolean b = (2 * 4) == a;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    // -----

    @Test
    fun `!= two variables 1`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int b = 123;
            boolean c = a != b;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two variables 2`() {
        assertThatThrownBy {
            """
            boolean a = true;
            int b = 123;
            boolean c = b != a;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two literals 1`() {
        assertThatThrownBy {
            """
            boolean a = 123 != false;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two literals 2`() {
        assertThatThrownBy {
            """
            boolean a = false != 123;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two complex expressions 1`() {
        assertThatThrownBy {
            """
            boolean a = true;
            boolean b = a != (2 * 4);
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }

    @Test
    fun `!= two complex expressions 2`() {
        assertThatThrownBy {
            """
            boolean a = true;
            boolean b = (2 * 4) != a;
        """.runInMainFunction()
        }.isInstanceOf(InvalidBinaryOperationException::class.java)
    }
}