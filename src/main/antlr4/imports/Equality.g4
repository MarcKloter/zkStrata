parser grammar Equality;

equality : (witness_var | instance_var) K_IS K_EQUAL K_TO (witness_var | instance_var)
         | (witness_var | instance_var) EQ (witness_var | instance_var) ;
