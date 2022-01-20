grammar DependencySpecification ;

WSP : [ \t]+;

LETTER        : 'A'..'Z' | 'a'..'z' ;
DIGIT         : '0'..'9' ;

ENV_VAR       : WSP* ('python_version' | 'python_full_version' |
                 'os_name' | 'sys_platform' | 'platform_release' |
                 'platform_system' | 'platform_version' |
                 'platform_machine' | 'platform_python_implementation' |
                 'implementation_name' | 'implementation_version' |
                 'extra' ) ;
VERSION_CMP   : WSP* ('<=' | '<' | '!=' | '==' | '>=' | '>' | '~=' | '===') ;
IDENT_END     : LETTER | DIGIT | (('-' | '_' | '.' )* (LETTER | DIGIT)) ;
IDENT         : (LETTER | DIGIT) IDENT_END* ;

version       : WSP* (IDENT | LETTER | DIGIT | '-' | '_' | '.' | '*' | '+' | '!' )+ ;
versionCmp    : VERSION_CMP ;
versionOne    : versionCmp version WSP* ;
versionMany   : versionOne (WSP* ',' versionOne)* ;
versionspec   : ('(' versionMany ')') | versionMany ;
markerOp      : VERSION_CMP | (WSP+ 'in' WSP+) | (WSP+ 'not' WSP+ 'in' WSP+) ;
PYTHON_STR_C  : (WSP | LETTER | DIGIT | '(' | ')' | '.' | '{' | '}' |
                 '-' | '_' | '*' | '#' | ':' | ';' | ',' | '/' | '?' |
                 '[' | ']' | '!' | '~' | '`' | '@' | '$' | '%' | '^' |
                 '&' | '=' | '+' | '|' | '<' | '>' ) ;
DQUOTE        : '"' ;
SQUOTE        : '\'' ;
PYTHON_STR    : (SQUOTE (PYTHON_STR_C | DQUOTE)* SQUOTE |
                 DQUOTE (PYTHON_STR_C | SQUOTE)* DQUOTE) ;

markerVar     : WSP* (ENV_VAR | PYTHON_STR) ;
markerExpr    : markerVar markerOp markerVar
              | WSP* '(' marker WSP* ')' ;
markerAnd     : markerExpr WSP* 'and' markerExpr
              | markerExpr ;
markerOr      : markerAnd WSP* 'or' markerAnd
              | markerAnd ;
marker        : markerOr ;
quotedMarker  : ';' WSP* marker ;

name          : IDENT ;
extrasList    : name (WSP* ',' WSP* name)* ;
extras        : '[' WSP* extrasList? WSP* ']' ;
nameReq       : name WSP* extras? WSP* versionspec? WSP* quotedMarker? ;
specification : WSP* ( nameReq ) WSP*  ('\n')* ;









/** Untested and unnecessary for METADATA part for URL based lookup:

urlspec       : '@' WSP* uri_reference ;
url_req       : name WSP* extras? WSP* urlspec WSP+ quotedMarker? ;
uri_reference : uri | relative_ref ;
uri           : scheme ':' hier_part ('?' query )? ( '#' uri_fragment)? ;
hier_part     : ('//' authority path_abempty) | path_absolute | path_rootless | path_empty ;
absolute_uri  : scheme ':' hier_part ( '?' query )? ;
relative_ref  : relative_part ( '?' query )? ( '#' uri_fragment )? ;
relative_part : '//' authority path_abempty | path_absolute | path_noscheme | path_empty ;
scheme        : LETTER ( LETTER | DIGIT | '+' | '-' | '.')* ;
authority     : ( userinfo '@' )? host ( ':' port )? ;
userinfo      : ( UNRESERVED | PCT_ENCODED | SUB_DELIMS | ':')* ;
host          : ip_literal | ip_v4address | reg_name ;
port          : DIGIT* ;
ip_literal    : '[' ( ip_v6address | ip_vFuture) ']' ;
ip_vFuture     : 'v' HEXDIG+ '.' ( UNRESERVED | SUB_DELIMS | ':')+ ;
ip_v6address   : (
                  ( h16 ':'){6} ls32
                  | '::' ( h16 ':'){5} ls32
                  | ( h16 )?  '::' ( h16 ':'){4} ls32
                  | ( ( h16 ':')? h16 )? '::' ( h16 ':'){3} ls32
                  | ( ( h16 ':'){0,2} h16 )? '::' ( h16 ':'){2} ls32
                  | ( ( h16 ':'){0,3} h16 )? '::' h16 ':' ls32
                  | ( ( h16 ':'){0,4} h16 )? '::' ls32
                  | ( ( h16 ':'){0,5} h16 )? '::' h16
                  | ( ( h16 ':'){0,6} h16 )? '::'
                  ) ;
h16           : HEXDIG{1,4} ;
ls32          : ( h16 ':' h16) | ip_v4address ;
ip_v4address   : dec_octet '.' dec_octet '.' dec_octet '.' dec_octet ;
nz            : ~'0' DIGIT ;
dec_octet     : ( 
                  DIGIT 
                  | nz DIGIT 
                  | '1' DIGIT{2} 
                  | '2' ('0' | '1' | '2' | '3' | '4') DIGIT
                  | '25' ('0' | '1' | '2' | '3' | '4' | '5') 
                  ) ;
reg_name : ( UNRESERVED | PCT_ENCODED | SUB_DELIMS)* ;
path : ( 
        path_abempty
        | path_absolute
        | path_noscheme
        | path_rootless
        | path_empty 
        ) ;
path_abempty  : ( '/' segment)* ;
path_absolute : '/' ( segment_nz ( '/' segment)* )? ;
path_noscheme : segment_nz_nc ( '/' segment)* ;
path_rootless : segment_nz ( '/' segment)* ;
path_empty    : PCHAR{0} ;
segment       : PCHAR* ;
segment_nz    : PCHAR+ ;
segment_nz_nc : ( UNRESERVED | PCT_ENCODED | SUB_DELIMS | '@')+ ;

query         : ( PCHAR | '/' | '?')* ;
uri_fragment  : ( PCHAR | '/' | '?')* ;
PCHAR         : UNRESERVED | PCT_ENCODED | SUB_DELIMS | ':' | '@' ;
PCT_ENCODED   : '%' HEXDIG ;
UNRESERVED    : LETTER | DIGIT | '-' | '.' | '_' | '~' ;
RESERVED      : GEN_DELIMS | SUB_DELIMS ;
GEN_DELIMS    : ':' | '/' | '?' | '#' | '(' | ')?' | '@' ;
SUB_DELIMS    : '!' | '$' | '&' | '\'' | '(' | ')' | '*' | '+' | ',' | ';' | '=' ;
HEXDIG        : DIGIT | 'a' | 'A' | 'b' | 'B' | 'c' | 'C' | 'd' | 'D' | 'e' | 'E' | 'f' | 'F' ;

*/




