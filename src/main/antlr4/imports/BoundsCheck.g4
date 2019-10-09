parser grammar BoundsCheck;

bounds_check : max_min | min_max | less_than | greater_than ;
max_min      : witness_var K_IS K_LESS K_THAN instance_var K_AND K_GREATER K_THAN instance_var ;
min_max      : witness_var K_IS K_GREATER K_THAN instance_var K_AND K_LESS K_THAN instance_var ;
less_than    : witness_var K_IS K_LESS K_THAN instance_var ;
greater_than : witness_var K_IS K_GREATER K_THAN instance_var ;
