parser grammar BoundsCheck;

bounds_check : max_min | min_max | min | max ;
max_min      : witness_var K_IS K_LESS K_THAN K_OR K_EQUAL K_TO instance_var K_AND K_GREATER K_THAN K_OR K_EQUAL K_TO instance_var
             | witness_var LT_EQ instance_var K_AND GT_EQ instance_var ;
min_max      : witness_var K_IS K_GREATER K_THAN K_OR K_EQUAL K_TO instance_var K_AND K_LESS K_THAN K_OR K_EQUAL K_TO instance_var
             | witness_var GT_EQ instance_var K_AND LT_EQ instance_var ;
min          : witness_var K_IS K_GREATER K_THAN K_OR K_EQUAL K_TO instance_var
             | witness_var GT_EQ instance_var ;
max          : witness_var K_IS K_LESS K_THAN K_OR K_EQUAL K_TO instance_var
             | witness_var LT_EQ instance_var ;
