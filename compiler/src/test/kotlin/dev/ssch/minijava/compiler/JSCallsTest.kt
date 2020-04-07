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

    @Test
    fun `call instance method without parameters from JS`() {
        val output = Source.withMiniJava("""
            int a;
            int b;
            
            Main(int a, int b) {
                this.a = a;
                this.b = b;
            }
            
            public static void main() {
                call(new Main(123, 456));
            }
            
            native static void call(Main m);
            
            public void callMe() {
                Console.println("callMe() called");
                Console.println("a = " + a);
                Console.println("b = " + b);
            }
        """).andJavaScript("""
            module.exports = runtime => {
                return {
                    "Main.call#Main": mainRef => {
                        const main = runtime.wasmDeref(mainRef);
                        runtime.instanceMethod(main, "callMe")();
                    }
                };
            };
        """).compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly(
            "callMe() called",
            "a = 123",
            "b = 456",
            "")
    }

    @Test
    fun `call instance method with parameters from JS`() {
        val output = Source.withMiniJava("""
            int a;
            int b;
            
            Main(int a, int b) {
                this.a = a;
                this.b = b;
            }
            
            public static void main() {
                Main m = new Main(123, 456);
                call(m);
                Console.println("a = " + m.a);
                Console.println("b = " + m.b);
            }
            
            native static void call(Main m);
            
            public void callMe(int a, int b) {
                Console.println("callMe(" + a + ", " + b + ") called");
                Console.println("a = " + this.a);
                Console.println("b = " + this.b);
                this.a = a;
                this.b = b;
            }
        """).andJavaScript("""
            module.exports = runtime => {
                return {
                    "Main.call#Main": mainRef => {
                        const main = runtime.wasmDeref(mainRef);
                        runtime.instanceMethod(main, "callMe", "int", "int")(111, 222);
                    }
                };
            };
        """).compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly(
            "callMe(111, 222) called",
            "a = 123",
            "b = 456",
            "a = 111",
            "b = 222",
            "")
    }

}