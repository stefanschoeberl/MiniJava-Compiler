grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (method)*;

method: (publicmodifier='public')? returntype=IDENT name=IDENT '(' ')' '{' (statements+=statement)* '}';

statement: completeStatement
         | incompleteIfStatement
         ;

completeStatement: 'if' '(' condition=expr ')' thenbranch=completeStatement 'else' elsebranch=completeStatement # CompleteIfElse
                 | 'while' '(' condition=expr ')' body=statement                                                # WhileLoop
                 | '{' (statement)* '}'                                                                         # Block
                 | type=IDENT name=IDENT '=' expr ';'                                                           # Vardeclassign
                 | type=IDENT name=IDENT ';'                                                                    # Vardecl
                 | name=IDENT '=' expr ';'                                                                      # Varassign
                 | name=IDENT '(' (parameters+=expr)* ')' ';'                                                                 # Call
                 ;

incompleteIfStatement: 'if' '(' condition=expr ')' thenbranch=statement                                                 # IncompleteIf
                     | 'if' '(' condition=expr ')' thenbranch=completeStatement 'else' elsebranch=incompleteIfStatement # IncompleteIfElse
                     ;

// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html

expr: '-' expr                              # MinusExpr
    | left=expr op=(MUL|DIV) right=expr     # MulDivExpr
    | left=expr op=(ADD|SUB) right=expr     # AddSubExpr
    | left=expr op=(LT|LE|GT|GE) right=expr # RelationalExpr
    | left=expr op=(EQ|NEQ) right=expr      # EqNeqExpr
    | left=expr op=AND right=expr           # AndExpr
    | left=expr op=OR right=expr            # OrExpr
    | IDENT                                 # IdExpr
    | INT                                   # IntExpr
    | value=(TRUE|FALSE)                    # BoolExpr
    | '(' expr ')'                          # ParensExpr
    ;

EQ:  '==';
NEQ: '!=';
LT: '<';
LE: '<=';
GT: '>';
GE: '>=';
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