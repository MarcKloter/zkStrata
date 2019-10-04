parser grammar MiMCHash;

mimc_hash  : witness_var K_IS K_PREIMAGE K_OF (witness_var | instance_var) ;
