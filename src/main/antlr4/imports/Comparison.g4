parser grammar Comparison;

comparison   : less_than | greater_than ;
less_than    : witness_var K_IS K_LESS K_THAN (witness_var | instance_var) ;
greater_than : witness_var K_IS K_GREATER K_THAN (witness_var | instance_var) ;