grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (statement)*;

statement: completeStatement
         | incompleteIfStatement
         ;

completeStatement: 'if' '(' condition=expr ')' thenbranch=completeStatement 'else' elsebranch=completeStatement # CompleteIfElse
                 | 'while' '(' condition=expr ')' body=statement                                                # WhileLoop
                 | '{' (statement)* '}'                                                                         # Block
                 | type=IDENT name=IDENT '=' expr ';'                                                           # Vardeclassign
                 | type=IDENT name=IDENT ';'                                                                    # Vardecl
                 | name=IDENT '=' expr ';'                                                                      # Varassign
                 | 'println' '(' expr ')' ';'                                                                   # Println
                 ;

incompleteIfStatement: 'if' '(' condition=expr ')' thenbranch=statement                                                 # IncompleteIf
                     | 'if' '(' condition=expr ')' thenbranch=completeStatement 'else' elsebranch=incompleteIfStatement # IncompleteIfElse
                     ;

// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html

expr: '-' expr                          # Minus
    | left=expr op=(MUL|DIV) right=expr # MulDiv
    | left=expr op=(ADD|SUB) right=expr # AddSub
    | left=expr op=(EQ|NEQ) right=expr  # EqNeq
    | left=expr op=AND right=expr       # And
    | left=expr op=OR right=expr        # Or
    | IDENT                             # Id
    | INT                               # Int
    | value=(TRUE|FALSE)                # Bool
    | '(' expr ')'                      # Parens
    ;

EQ:  '==';
NEQ: '!=';
AND: '&&';
OR: '||';

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';

TRUE: 'true';
FALSE: 'false';
IDENT: [_a-zA-Z][_a-zA-Z0-9]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;