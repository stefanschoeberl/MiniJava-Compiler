grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: statement*;

statement: printstatement ';';

printstatement: 'print' '(' INT ')';

INT: [0-9]+;