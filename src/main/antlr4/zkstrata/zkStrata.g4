parser grammar zkStrata;

options { tokenVocab = zkStrataLexer; }

import BoundsCheck,
       Equality,
       Inequality,
       MerkleTree,
       MiMCHash;

statement           : K_PROOF K_FOR (subjects | K_THIS) K_THAT predicates EOF ;
predicates          : predicate_clause (joint predicate_clause)* ;
subjects            : subject (joint subject)* ;
subject             : K_INSTANCE? schema_name K_AS alias ;

witness_var         : referenced_value ;
instance_var        : literal_value ;

referenced_value    : alias (DOT property)+ ;

literal_value       : STRING_LITERAL
                    | INTEGER_LITERAL
                    | HEX_LITERAL ;

alias               : IDENTIFIER ;
schema_name         : IDENTIFIER ;
property            : IDENTIFIER ;

joint               : (K_AND | SCOL) ;

/**
 * Predicate Clauses
 */
predicate_clause    : bounds_check
                    | merkle_tree
                    | mimc_hash
                    | equality
                    | inequality ;
