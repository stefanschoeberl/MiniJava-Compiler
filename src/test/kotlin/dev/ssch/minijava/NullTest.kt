package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NullTest : CompilerTest() {

    @Test
    fun `assign null (class)`() {
        val output = """
            class Main {
                public static void main() {
                    Main m = null;
                    Console.println(123);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `assign null (array)`() {
        val output = """
            class Main {
                public static void main() {
                    int[] a = null;
                    Console.println(123);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `print null (class)`() {
        val output = Source.withMiniJava("""
            class Main {
                int x;
                public static void main() {
                    Main a = new Main();
                    a.x = 123;
                    Main b = null;
                    println(a);
                    println(b);
                }
                native static void println(Main m);
            }
        """).andJavaScript("""
            module.exports = function(runtime) {
                return {
                    "Main.println#Main": function(mRef) {
                        const m = runtime.wasmDeref(mRef);
                        console.log(m);
                    }
                }
            }
        """.trimIndent()).compileAndRun()
        assertThat(output.lines()).containsExactly("{ x: 123 }", "null", "")
    }

    @Test
    fun `print null (array)`() {
        val output = """
            class Main {
                public static void main() {
                    int[] a = null;
                    Console.println(a);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("null", "")
    }
}