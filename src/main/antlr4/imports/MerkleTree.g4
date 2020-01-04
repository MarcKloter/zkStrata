parser grammar MerkleTree;

merkle_tree : (witness_var | instance_var) K_IS K_MERKLE K_ROOT K_OF subtree;
subtree     : LPAREN (subtree | leaf) COMMA (subtree | leaf) RPAREN;
leaf        : (witness_var | instance_var) ;
