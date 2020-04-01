package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.UnknownTypeException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UnknownTypeExceptionTest : CompilerTest() {

    @Test
    fun `declare variable of unknown type`() {
        assertThatThrownBy {
        """
            xyz a;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UnknownTypeException::class.java)
    }

    @Test
    fun `declare and initialize variable of unknown type`() {
        assertThatThrownBy {
        """
            xyz a = 123;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(UnknownTypeException::class.java)
    }

    @Test
    fun `declare function with unknown type`() {
        assertThatThrownBy {
        """
            void a(xyz b) {}
            public void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(UnknownTypeException::class.java)
    }


}