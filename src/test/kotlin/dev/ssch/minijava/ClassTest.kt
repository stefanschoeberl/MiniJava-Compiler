package dev.ssch.minijava

import dev.ssch.minijava.exception.RedefinedClassException
import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClassTest : CompilerTest() {

    @Test
    fun `call static method of other class`() {
        val output = """
            class Main {
                public static void main() {
                    Other.doSomething(123);
                }
            }
            
            class Other {
                static void doSomething(int x) {
                    Console.println(x);
                }
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `redefine class`() {
        Assertions.assertThatThrownBy {
            """
            class A {}
            class A {}
        """.compileAndRunMainFunction()
        }.isInstanceOf(RedefinedClassException::class.java)
    }
}