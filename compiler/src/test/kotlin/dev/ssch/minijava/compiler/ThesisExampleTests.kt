package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ThesisExampleTests : CompilerTest() {

    @Test
    fun `simple test`() {
        val output = """
            public static void main() { 
                Console.println(123);
            }
            """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123", "")
    }

    @Test
    fun `native test`() {
        val output = Source.withMiniJava("""
            native static void println(int x);
            public static void main() { 
                Main.println(123);
            }
            """).andJavaScript("""
            module.exports = (runtime) => {
                return {
                    "Main.println#int": (x) => console.log(x)
                };
            };
            """).compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123", "")
    }
}