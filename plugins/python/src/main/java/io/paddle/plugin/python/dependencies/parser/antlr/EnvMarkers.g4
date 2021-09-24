grammar EnvMarkers;

WS : [ \t]+ -> skip ;

VERSION_CMP    : '<' | '<=' | '!=' | '==' | '>=' | '>' | '~=' | '===' ;

PYTHON_STR_C   : (WS | LETTER | DIGIT | '(' | ')' | '.' | '{' | '}' |
                 '-' | '_' | '*' | '#' | ':' | ';' | ',' | '/' | '?' |
                 '[' | ']' | '!' | '~' | '`' | '@' | '$' | '%' | '^' |
                 '&' | '=' | '+' | '|' | '<' | '>' ) ;

markerOp       : VERSION_CMP | IN | ( NOT  IN ) ;

Dquote        : '"' ;
Squote        : '\'' ;
PythonStr     : (Squote (PYTHON_STR_C | Dquote)* Squote |
                 Dquote (PYTHON_STR_C | Squote)* Dquote) ;
envVar        : ('python_version' | 'python_full_version' |
                 'os_name' | 'sys_platform' | 'platform_release' |
                 'platform_system' | 'platform_version' |
                 'platform_machine' | 'platform_python_implementation' |
                 'implementation_name' | 'implementation_version' |
                 'extra' ) ;
markerVar     : envVar | PythonStr ;
markerExpr    : markerVar  markerOp  markerVar
              | '('  marker  ')' ;
markerAnd     : markerExpr ( 'and'  markerExpr)? ;
markerOr      : markerAnd ( 'or'  markerAnd)? ;
marker        : markerOr ;

quotedMarker  : ';'  marker  ('\n')* ;

LETTER        : 'A'..'Z' | 'a'..'z' ;
DIGIT         : '0'..'9' ;
NOT           : 'not' ;
IN            : 'in' ;
