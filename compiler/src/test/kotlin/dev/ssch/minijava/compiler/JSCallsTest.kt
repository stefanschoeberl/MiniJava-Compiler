package dev.ssch.minijava.compiler

import dev.ssch.minijava.compiler.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JSCallsTest : CompilerTest() {

    @Test
    fun `call static method without parameters from JS`() {
        val output = Source.withMiniJava("""
            public static void main() {
                call();
            }
            
            native static void call();
            
            public static void callMe() {
                Console.println("callMe() called");
            }
        """).andJavaScript("""
            module.exports = runtime => {
                return {
                    "Main.call": () => {
                        runtime.staticMethod("Main", "callMe")();
                    }
                };
            };
        """).compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("callMe() called","")
    }

    @Test
    fun `call static method with parameters from JS`() {
        val output = Source.withMiniJava("""
            public static void main() {
                call();
            }
            
            native static void call();
            
            public static void callMe(int a, int b) {
                Console.println("callMe(" + a + ", " + b + ") called");
            }
        """).andJavaScript("""
            module.exports = runtime => {
                return {
                    "Main.call": () => {
                        runtime.staticMethod("Main", "callMe", "int", "int")(4, 7);
                    }
                };
            };
        """).compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("callMe(4, 7) called","")
    }

}