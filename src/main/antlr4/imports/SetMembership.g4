parser grammar SetMembership;

set_membership : (witness_var | instance_var) K_IS K_MEMBER K_OF set ;
set            : LPAREN (witness_var | instance_var) (COMMA (witness_var | instance_var))+ RPAREN ;
