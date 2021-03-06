lexer grammar zkStrataLexer;

/**
 * Keywords
 */
K_AS                  : A S ;
K_AND                 : A N D ;
K_COMPLIANT           : C O M P L I A N T ;
K_EQUAL               : E Q U A L ;
K_FOR                 : F O R ;
K_GREATER             : G R E A T E R ;
K_INSTANCE            : I N S T A N C E ;
K_IS                  : I S ;
K_LESS                : L E S S ;
K_MEMBER              : M E M B E R ;
K_MERKLE              : M E R K L E ;
K_OF                  : O F ;
K_OR                  : O R ;
K_PREIMAGE            : P R E I M A G E ;
K_PROOF               : P R O O F ;
K_ROOT                : R O O T ;
K_THAN                : T H A N ;
K_THAT                : T H A T ;
K_THIS                : T H I S ;
K_TO                  : T O ;
K_UNEQUAL             : U N E Q U A L ;
K_WITNESS             : W I T N E S S ;

/**
 * Separators
 */
LPAREN  : '(' ;
RPAREN  : ')' ;
COMMA   : ',' ;
DOT     : '.' ;
SCOL    : ';' ;
EQ      : '=' ;
NEQ     : '!=' ;
LT      : '<' ;
LT_EQ   : '<=' ;
GT      : '>' ;
GT_EQ   : '>=' ;

/**
 * zkStrata Lexer Rules
 */
NEWLINES            : ('\r'? '\n' | '\r')+ -> skip ;
SPACES              : [ \u000B\t]          -> channel(HIDDEN) ;
COMMENTS            : '/*' .*? '*/'        -> skip ;
LINE_COMMENTS       : '//' ~[\r\n]*        -> skip ;

STRING_LITERAL      : '\'' ( ~'\'' | '\'\'' )* '\'';
INTEGER_LITERAL     : DIGIT+;
HEX_LITERAL         : '0x' HEX_DIGIT+;
BOOLEAN_LITERAL     : T R U E | F A L S E ;

IDENTIFIER          : [a-zA-Z]([a-zA-Z0-9_])* ;
CONSTANT            : [_] IDENTIFIER ;

/**
 * Fragments
 */
fragment HEX_DIGIT  : [a-fA-F0-9];
fragment DIGIT      : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
