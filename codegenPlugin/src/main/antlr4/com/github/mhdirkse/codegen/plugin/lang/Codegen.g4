grammar Codegen;

prog : statement ( ';' statement )* ';'? EOF ;

statement
  : 'input' fullClassNameInput # inputStatement
  | 'output' fullClassNameOutput # outputStatement
  | 'implement' source 'chain' target 'with' 'handler' handler # chainStatement
  | 'implement' source 'in' target # implementStatement
  ;

fullClassNameInput : FULL_CLASS_NAME ;
fullClassNameOutput : FULL_CLASS_NAME ;

source : ID ;
target : ID ;
handler : ID ;

ID : [a-zA-Z] [a-zA-Z0-9]* ;
FULL_CLASS_NAME : ID ('.' ID)* ;

WS : [ \t\r\n] -> skip ;