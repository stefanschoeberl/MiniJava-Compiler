package dev.ssch.minijava.compiler.exceptions

import dev.ssch.minijava.compiler.exception.InvalidMethodBodyException
import dev.ssch.minijava.compiler.exception.InvalidModifierException
import dev.ssch.minijava.compiler.exception.MissingMethodBodyException
import dev.ssch.minijava.compiler.exception.VoidParameterException
import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MethodExceptionTest : CompilerTest() {

    @Test
    fun `public public`() {
        assertThatThrownBy {
        """
            public public void a() {}
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `native native`() {
        assertThatThrownBy {
        """
            native native void a() {}
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `static static`() {
        assertThatThrownBy {
        """
            static static void a() {}
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `public native public`() {
        assertThatThrownBy {
        """
            public native public void a() {}
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `native public native`() {
        assertThatThrownBy {
        """
            native public native void a() {}
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `native method with body`() {
        assertThatThrownBy {
        """
            native void a() {}
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(InvalidMethodBodyException::class.java)
    }

    @Test
    fun `normal method without body`() {
        assertThatThrownBy {
        """
            public void a();
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(MissingMethodBodyException::class.java)
    }

    @Test
    fun `void parameter`() {
        assertThatThrownBy {
            """
            public static void a() { }
            
            public static void b(int a) {}
            
            public static void c() {
                b(a());
            }
        """.compileAndRunInMainClass(false)
        }.isInstanceOf(VoidParameterException::class.java)
    }


}