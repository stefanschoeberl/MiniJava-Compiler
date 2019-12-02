package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.RedefinedMethodException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RedefinedMethodExceptionTest : CompilerTest() {

    @Test
    fun `redefine method 1 (same return type)`() {
        assertThatThrownBy {
        """
            void a() {}
            void a() {}
            public void main() {}
        """.compileAndRunMainFunction()
        }.isInstanceOf(RedefinedMethodException::class.java)
    }

    @Test
    fun `redefine method 2 (same return type)`() {
        assertThatThrownBy {
            """
            int a() {}
            int a() {}
            public void main() {}
        """.compileAndRunMainFunction()
        }.isInstanceOf(RedefinedMethodException::class.java)
    }

    @Test
    fun `redefine method (different return type)`() {
        assertThatThrownBy {
            """
            boolean a() {}
            int a() {}
            public void main() {}
        """.compileAndRunMainFunction()
        }.isInstanceOf(RedefinedMethodException::class.java)
    }
}