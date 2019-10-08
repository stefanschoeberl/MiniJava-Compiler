grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (statement ';')*;

statement: 'int' IDENT            # vardecl
         | IDENT '=' expr         # varassign
         | 'println' '(' expr ')' # println
         ;

expr: IDENT         # id
    | INT           # int
    ;

IDENT: [_a-zA-Z][_a-zA-Z0-9]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;