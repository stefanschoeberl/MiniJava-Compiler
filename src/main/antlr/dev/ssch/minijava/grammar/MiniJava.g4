grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (statement ';')*;

statement: 'int' IDENT '=' expr   # Vardeclassign
         | 'int' IDENT            # Vardecl
         | IDENT '=' expr         # Varassign
         | 'println' '(' expr ')' # Println
         ;

expr: expr op=(PLUS|MINUS) expr # AddSub
    | IDENT                     # Id
    | INT                       # Int
    | '(' expr ')'              # Parens
    ;

PLUS: '+';
MINUS: '-';

IDENT: [_a-zA-Z][_a-zA-Z0-9]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;