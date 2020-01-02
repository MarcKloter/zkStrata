parser grammar Comparison;

comparison      : less_than | less_than_eq | greater_than | greater_than_eq ;
less_than       : (witness_var | instance_var) K_IS K_LESS K_THAN (witness_var | instance_var)
                | (witness_var | instance_var) LT (witness_var | instance_var) ;
less_than_eq    : (witness_var | instance_var) K_IS K_LESS K_THAN K_OR K_EQUAL K_TO (witness_var | instance_var)
                | (witness_var | instance_var) LT_EQ (witness_var | instance_var) ;
greater_than    : (witness_var | instance_var) K_IS K_GREATER K_THAN (witness_var | instance_var)
                | (witness_var | instance_var) GT (witness_var | instance_var) ;
greater_than_eq : (witness_var | instance_var) K_IS K_GREATER K_THAN K_OR K_EQUAL K_TO (witness_var | instance_var)
                | (witness_var | instance_var) GT_EQ (witness_var | instance_var) ;