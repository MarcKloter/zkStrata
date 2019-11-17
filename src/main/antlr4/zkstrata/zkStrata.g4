parser grammar zkStrata;

options { tokenVocab = zkStrataLexer; }

import BoundsCheck,
       Equality,
       Inequality,
       MerkleTree,
       MiMCHash,
       Comparison,
       SetMembership;

statement           : K_PROOF K_FOR subjects K_THAT predicates EOF ;
predicates          : predicate_clause (joint predicate_clause)* (joint)? ;
subjects            : subject (joint subject)* (joint)? ;
subject             : K_INSTANCE? schema_name K_AS alias
                    | K_THIS ;

witness_var         : referenced_value ;
instance_var        : (referenced_value | literal_value) ;

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
                    | inequality
                    | comparison
                    | set_membership ;
