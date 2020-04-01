package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.ThisReferencedFromStaticContextException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ThisReferencedFromStaticContextExceptionTest : CompilerTest() {

    @Test
    fun `use this in static method`() {
        assertThatThrownBy {
        """
            Main m = this;
        """.compileAndRunInMainFunction()
        }.isInstanceOf(ThisReferencedFromStaticContextException::class.java)
    }
}