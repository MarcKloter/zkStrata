parser grammar zkStrata;

options { tokenVocab = zkStrataLexer; }

import BoundsCheck,
       Equality,
       Inequality,
       MerkleTree,
       MiMCHash,
       Comparison,
       SetMembership;

statement           : K_PROOF K_FOR subjects K_THAT predicate EOF ;
predicate           : clause (SCOL)? ;
subjects            : subject ((K_AND | SCOL) subject)* (SCOL)? ;
subject             : K_INSTANCE? schema K_AS alias
                    | (K_WITNESS | K_INSTANCE) alias K_COMPLIANT K_TO schema
                    | K_THIS ;

witness_var         : reference             # WitnessVariable ;
instance_var        : (reference | literal) # InstanceVariable ;

reference           : alias (DOT property)+ ;

literal             : STRING_LITERAL
                    | INTEGER_LITERAL
                    | HEX_LITERAL ;

alias               : IDENTIFIER ;
schema              : IDENTIFIER ;
property            : IDENTIFIER ;

clause              : LPAREN clause RPAREN          # ParenClause
                    | clause (K_AND | SCOL) clause  # AndClause
                    | clause K_OR clause            # OrClause
                    | predicate_clause              # AtomClause ;

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
