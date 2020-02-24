grammar MiniJava;

@header {
package dev.ssch.minijava.grammar;
}

minijava: (method)*;

method: (publicmodifier+='public' | nativemodifier+='native')* returntype=IDENT name=IDENT
        '(' (parameters+=formalParameter (',' parameters+=formalParameter)*)? ')'
        (block='{' (statements+=statement)* '}' | semicolon=';');

formalParameter: type=IDENT name=IDENT;

statement: completeStatement
         | incompleteIfStatement
         ;

completeStatement: 'if' '(' condition=expr ')' thenbranch=completeStatement 'else' elsebranch=completeStatement # CompleteIfElseStmt
                 | 'while' '(' condition=expr ')' body=statement                                                # WhileLoopStmt
                 | '{' (statement)* '}'                                                                         # BlockStmt
                 | type=IDENT name=IDENT '=' expr ';'                                                           # VardeclassignStmt
                 | type=IDENT name=IDENT ';'                                                                    # VardeclStmt
                 | name=IDENT '=' expr ';'                                                                      # VarassignStmt
                 | callExpression ';'                                                                           # CallStmt
                 | 'return' value=expr ';'                                                                      # ReturnStmt
                 ;

incompleteIfStatement: 'if' '(' condition=expr ')' thenbranch=statement                                                 # IncompleteIfStmt
                     | 'if' '(' condition=expr ')' thenbranch=completeStatement 'else' elsebranch=incompleteIfStatement # IncompleteIfElseStmt
                     ;

// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html

expr: '-' expr                              # MinusExpr
    | '(' type=IDENT ')' expr               # CastExpr
    | left=expr op=(MUL|DIV) right=expr     # MulDivExpr
    | left=expr op=(ADD|SUB) right=expr     # AddSubExpr
    | left=expr op=(LT|LE|GT|GE) right=expr # RelationalExpr
    | left=expr op=(EQ|NEQ) right=expr      # EqNeqExpr
    | left=expr op=AND right=expr           # AndExpr
    | left=expr op=OR right=expr            # OrExpr
    | IDENT                                 # IdExpr
    | INT                                   # IntExpr
    | FLOAT                                 # FloatExpr
    | value=(TRUE|FALSE)                    # BoolExpr
    | callExpression                        # CallExpr
    | '(' expr ')'                          # ParensExpr
    ;

callExpression:
    name=IDENT '(' (parameters+=expr (',' parameters+=expr)*)? ')';

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
FLOAT: [0-9]+ ('.' [0-9]*)? 'f';
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;
SINGLE_LINE_COMMENT: '//' .*? [\r\n]+ -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;