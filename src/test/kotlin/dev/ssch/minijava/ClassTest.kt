package dev.ssch.minijava

import dev.ssch.util.CompilerTest
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



}