package dev.ssch.minijava

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
        }
    }

    @Test
    fun `declare fields`() {
        val output = """
            class Main {
                public static void main() {
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `declare variable of class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p;
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `instatiate class`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point();
                    Console.println(123);
                }
            }
            
            class Point {
                int x;
                int y;
            }
        """.compileAndRunMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

}