grammar Codegen;

prog : statement ( ';' statement )* ';'? EOF ;

statement
  : 'input' fullClassNameInput # inputStatement
  | 'output' fullClassNameOutput # outputStatement
  | 'from' source 'generate' target 'methods' methods # generateInterfaceStatement
  | 'from' source 'super' target 'methods' methods # superInterfaceStatement
  | 'implement' source 'chain' target 'with' 'handler' handler # chainStatement
  | 'implement' source 'in' target # implementStatement
  | 'properties' 'of' propertyOwner ':' propertyDef ( ',' propertyDef) * constructor? # propertyDefStatement
  ;

fullClassNameInput : FULL_CLASS_NAME ;
fullClassNameOutput : FULL_CLASS_NAME ;
methods : method+ ;
propertyDef : ((property+) | allProperties) (getter | setter)* ;
getter : 'getter' accessModifier ;
setter : 'setter' accessModifier ;
constructor : 'constructor' property+ ;

source : ID ;
target : ID ;
handler : ID ;
method : ID ;
propertyOwner : ID ;
property : ID ;
allProperties : 'all' ;
accessModifier : 'public' | 'protected' | 'package' | 'private' ;

ID : [a-zA-Z] [a-zA-Z0-9]* ;
FULL_CLASS_NAME : ID ('.' ID)* ;
NUMBER : [1-9][0-9]* ;
WS : [ \t\r\n] -> skip ;