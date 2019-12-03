package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.UnknownTypeException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UnknownTypeExceptionTest : CompilerTest() {

    @Test
    fun `declare variable of unknown type`() {
        assertThatThrownBy {
        """
            xyz a;
        """.runInMainFunction()
        }.isInstanceOf(UnknownTypeException::class.java)
    }

    @Test
    fun `declare and initialize variable of unknown type`() {
        assertThatThrownBy {
        """
            xyz a = 123;
        """.runInMainFunction()
        }.isInstanceOf(UnknownTypeException::class.java)
    }

    @Test
    fun `declare function with unknown type`() {
        assertThatThrownBy {
        """
            void a(xyz b) {}
            public void main() {}
        """.compileAndRunMainFunction()
        }.isInstanceOf(UnknownTypeException::class.java)
    }


}