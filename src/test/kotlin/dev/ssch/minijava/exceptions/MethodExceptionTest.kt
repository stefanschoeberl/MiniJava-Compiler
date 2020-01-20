package dev.ssch.minijava.exceptions

import dev.ssch.minijava.exception.InvalidMethodBodyException
import dev.ssch.minijava.exception.InvalidModifierException
import dev.ssch.minijava.exception.MissingMethodBodyException
import dev.ssch.minijava.exception.VoidParameterException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MethodExceptionTest : CompilerTest() {

    @Test
    fun `public public`() {
        assertThatThrownBy {
        """
            public public void a() {}
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `native native`() {
        assertThatThrownBy {
        """
            native native void a() {}
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `public native public`() {
        assertThatThrownBy {
        """
            public native public void a() {}
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `native public native`() {
        assertThatThrownBy {
        """
            native public native void a() {}
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(InvalidModifierException::class.java)
    }

    @Test
    fun `native method with body`() {
        assertThatThrownBy {
        """
            native void a() {}
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(InvalidMethodBodyException::class.java)
    }

    @Test
    fun `normal method without body`() {
        assertThatThrownBy {
        """
            public void a();
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(MissingMethodBodyException::class.java)
    }

    @Test
    fun `void parameter`() {
        assertThatThrownBy {
            """
            public void a() { }
            
            public void b(int a) {}
            
            public void c() {
                b(a());
            }
        """.compileAndRunMainFunction(false)
        }.isInstanceOf(VoidParameterException::class.java)
    }


}