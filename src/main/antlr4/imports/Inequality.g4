parser grammar Inequality;

inequality : (witness_var | instance_var) K_IS K_UNEQUAL K_TO (witness_var | instance_var)
           | (witness_var | instance_var) NEQ (witness_var | instance_var);
