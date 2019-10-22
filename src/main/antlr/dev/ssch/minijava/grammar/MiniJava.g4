grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (statement ';')*;

statement: type=IDENT name=IDENT '=' expr   # Vardeclassign
         | type=IDENT name=IDENT            # Vardecl
         | name=IDENT '=' expr              # Varassign
         | 'println' '(' expr ')'           # Println
         ;
// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html

expr: expr op=(MUL|DIV) expr # MulDiv
    | expr op=(ADD|SUB) expr # AddSub
    | expr op=EQ expr        # EqNeq
    | '-'? IDENT             # Id
    | '-'? INT               # Int
    | value=(TRUE|FALSE)     # Bool
    | '-'? '(' expr ')'      # Parens
    ;

EQ:  '==';
//NEQ: '!=';

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';

TRUE: 'true';
FALSE: 'false';
IDENT: [_a-zA-Z][_a-zA-Z0-9]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;