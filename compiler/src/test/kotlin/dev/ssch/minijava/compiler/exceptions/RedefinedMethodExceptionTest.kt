package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.RedefinedMethodException
import dev.ssch.minijava.compiler.util.CompilerTest
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
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedMethodException::class.java)
    }

    @Test
    fun `redefine method 2 (same return type)`() {
        assertThatThrownBy {
            """
            int a() {}
            int a() {}
            public void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedMethodException::class.java)
    }

    @Test
    fun `redefine method (different return type)`() {
        assertThatThrownBy {
            """
            boolean a() {}
            int a() {}
            public void main() {}
        """.compileAndRunInMainClass()
        }.isInstanceOf(RedefinedMethodException::class.java)
    }
}