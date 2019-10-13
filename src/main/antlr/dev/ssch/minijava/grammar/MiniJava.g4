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

expr: expr op=(MUL|DIV) expr # MulDiv
    | expr op=(ADD|SUB) expr # AddSub
    | IDENT                  # Id
    | INT                    # Int
    | '(' expr ')'           # Parens
    ;

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';

IDENT: [_a-zA-Z][_a-zA-Z0-9]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;