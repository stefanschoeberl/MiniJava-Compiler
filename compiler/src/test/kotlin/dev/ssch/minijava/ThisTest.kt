package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ThisTest : CompilerTest() {

    @Test
    fun `access fields in constructor with this`() {
        val output = """
            class Main {
                public static void main() {
                    Point p = new Point(123, 456);
                    Console.println(p.x);
                    Console.println(p.y);
                }
            }
            
            class Point {
                int x;
                int y;
                
                Point(int x, int y) {
                    this.x = x;
                    this.y = y;
                    Console.println(this.x);
                    Console.println(this.y);
                    this.x = this.y;
                    this.y = x;
                    Console.println(this.x);
                    Console.println(this.y);
                    this.y = this.x;
                    this.x = x;
                    Console.println(this.x);
                    Console.println(this.y);
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "456", "456", "123", "123", "456", "123", "456", "")
    }

    @Test
    fun `pass this around`() {
        val output = """
            class Main {
                public static void main() {
                    A a = new A();
                    Console.println(a.value);
                    Console.println(a.b.value);
                    Console.println(a.b.a.value);
                    Console.println(a.b.a.b.value);
                }
            }
            
            class A {
                B b;
                int value;
                
                A() {
                    b = new B(this);
                    value = 123;
                }
            }
            
            class B {
                A a;
                int value;
                
                B(A a) {
                    this.a = a;
                    value = 456;
                }
            }
        """.compileAndRun()
        assertThat(output.lines()).containsExactly("123", "456", "123", "456", "")
    }


}