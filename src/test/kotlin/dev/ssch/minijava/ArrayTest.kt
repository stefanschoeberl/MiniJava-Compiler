package dev.ssch.minijava

import dev.ssch.util.CompilerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ArrayTest : CompilerTest() {

    @Test
    fun `create array`() {
        val output = """
            int[] a = new int[1];
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Disabled
    @Test
    fun `write to array`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            println(111);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("111", "")
    }

    @Disabled
    @Test
    fun `write to and read from array with one element`() {
        val output = """
            int[] a = new int[1];
            a[0] = 123;
            println(a[0]);
        """.runInMainFunction()
        assertThat(output.lines()).containsExactly("123", "")
    }

    // TODO: Array as parameter, returnvalue
}