grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: statement*;

statement: printlnstatement ';';

printlnstatement: 'println' '(' INT ')';

INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;