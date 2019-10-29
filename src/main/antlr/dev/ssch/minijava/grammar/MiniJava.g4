grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (statement)*;

statement: '{' (statement)* '}'               # Block
         | type=IDENT name=IDENT '=' expr ';' # Vardeclassign
         | type=IDENT name=IDENT ';'          # Vardecl
         | name=IDENT '=' expr ';'            # Varassign
         | 'println' '(' expr ')' ';'         # Println
         ;
// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html

expr: '-' expr                          # Minus
    | left=expr op=(MUL|DIV) right=expr # MulDiv
    | left=expr op=(ADD|SUB) right=expr # AddSub
    | left=expr op=(EQ|NEQ) right=expr  # EqNeq
    | IDENT                             # Id
    | INT                               # Int
    | value=(TRUE|FALSE)                # Bool
    | '(' expr ')'                      # Parens
    ;

EQ:  '==';
NEQ: '!=';

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';

TRUE: 'true';
FALSE: 'false';
IDENT: [_a-zA-Z][_a-zA-Z0-9]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;