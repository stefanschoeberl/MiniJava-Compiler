package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InstanceMethodsTest : CompilerTest() {

    @Test
    fun `instance method without parameters`() {
        val output = """
            int x;
            
            Main(int x) {
                this.x = x;
            }
            
            public void square() {
                x = x * x;
            }
            
            public static void main() {
                Main data = new Main(5);
                data.square();
                Console.println(data.x);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("25","")
    }

    @Test
    fun `instance method with parameters`() {
        val output = """
            int x;
            
            Main(int x) {
                this.x = x;
            }
            
            public void add(int x) {
                this.x = this.x + x;
            }
            
            public static void main() {
                Main data = new Main(5);
                data.add(12);
                Console.println(data.x);
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("17","")
    }

    @Test
    fun `instance method with return value`() {
        val output = """
            int x;
            int y;
            
            Main(int x, int y) {
                this.x = x;
                this.y = y;
            }
            
            public int sum() {
                return x + y;
            }
            
            public static void main() {
                Main data = new Main(2, 3);
                Console.println(data.sum());
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("5","")
    }

    @Test
    fun `instance method with return value and parameters`() {
        val output = """
            int x;
            int y;
            
            Main(int x, int y) {
                this.x = x;
                this.y = y;
            }
            
            public int sum(int z) {
                return x + y + z;
            }
            
            public static void main() {
                Main data = new Main(2, 3);
                Console.println(data.sum(4));
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("9","")
    }

    @Test
    fun `instance method with return value and multiple parameters`() {
        val output = """
            int x;
            int y;
            
            Main(int x, int y) {
                this.x = x;
                this.y = y;
            }
            
            public int sum(int z, int a) {
                return x + y + z + a;
            }
            
            public static void main() {
                Main data = new Main(2, 3);
                Console.println(data.sum(4, 5));
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("14","")
    }

    @Test
    fun `call instance method on expression`() {
        val output = """
            class A {
                B b;
                
                public B getB() {
                    return b;
                }
            }
            
            class B {
                public void b() {
                    Console.println(123);
                }
            }

            class Main {
                public static void main() {
                    A a = new A();
                    a.b = new B();
                    a.b.b();
                    a.getB().b();
                }
            } 
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123","123", "")
    }

    @Test
    fun `call static method from instance method`() {
        val output = """
            public void a() {
                b();
                this.b();
            }
            
            public static void b() {
                Console.println(123);
            }
            
            public static void main() {
                Main main = new Main();
                main.a();
            }
        """.compileAndRunInMainClass()
        assertThat(output.lines()).containsExactly("123","123", "")
    }

    @Test
    fun `native instance method`() {
        val output = Source.withMiniJava("""
            class Point {
                int x;
                int y;
                
                native float calc(int offset);
            }
            
            class Main {
                public static void main() {
                    Point point = new Point();
                    point.x = 3;
                    point.y = 4;
                    Console.println(point.calc(2));
                }
            }
        """).andJavaScript("""
            module.exports = function (runtime) {
                return {
                    "Point.calc#int": function(thisRef, offset) {
                        const point = runtime.wasmDeref(thisRef);
                        return point.x + point.y + offset;
                    }
                }
            };
        """).compileAndRun()
        assertThat(output.lines()).containsExactly("9", "")
    }

}