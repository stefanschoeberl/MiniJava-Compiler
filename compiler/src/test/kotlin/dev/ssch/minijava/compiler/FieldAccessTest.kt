package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.exception.NotAReferenceTypeException
import dev.ssch.minijava.compiler.exception.UndefinedFieldException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class FieldAccessTest : CompilerTest() {

    @Test
    fun `write to field of non-reference type (int)`() {
        assertThatThrownBy {
        """
            int a = 123;
            a.value = 5;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `write to field of non-reference type (null)`() {
        assertThatThrownBy {
        """
            null.value = 5;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `write to field of non-reference type (void)`() {
        assertThatThrownBy {
        """
            main().value = 5;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `write to undefined field`() {
        assertThatThrownBy {
        """
            Main m = new Main();
            m.value = 123;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedFieldException::class.java)
    }

    @Test
    fun `read from field of non-reference type (int)`() {
        assertThatThrownBy {
        """
            int a = 123;
            Console.println(a.value);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `read from field of non-reference type (null)`() {
        assertThatThrownBy {
        """
            Console.println(null.value);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `read from field of non-reference type (void)`() {
        assertThatThrownBy {
        """
            Console.println(main().value);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(NotAReferenceTypeException::class.java)
    }

    @Test
    fun `read from undefined field`() {
        assertThatThrownBy {
        """
            Main m = new Main();
            Console.println(m.value);
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UndefinedFieldException::class.java)
    }
}