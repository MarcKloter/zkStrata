# zkStrata
[![build](https://github.com/MarcKloter/zkStrata/workflows/build/badge.svg)](https://github.com/MarcKloter/zkStrata/actions?query=workflow:build)
[![codecov](https://codecov.io/gh/MarcKloter/zkStrata/branch/master/graph/badge.svg)](https://codecov.io/gh/MarcKloter/zkStrata)
[![build](https://github.com/MarcKloter/zkStrata/workflows/user%20stories/badge.svg)](https://github.com/MarcKloter/zkStrata/actions?query=workflow:"user+stories")

zkStrata (**z**ero-**k**nowledge for **st**ructured d**ata**) is a declarative language for expressing zero-knowledge proofs over structured data. We denote a collection of data as structured, if there can be a specification provided describing available fields along with their data types (such as JSON, XML or SQL schemas). zkStrata provides type safety, improved readability through predominant use of elements from natural language and proving system independence of specified statements.

## Examples
There are the following examples with detailed walkthroughs available, make sure to have a look at the `README.md` files within the respective directories:

| Name | Directory | Status |
| ---- | --------- | ------ |
| Passport | [examples/passport/](examples/passport/) | [![build](https://github.com/MarcKloter/zkStrata/workflows/example%3A%20passport/badge.svg)](https://github.com/MarcKloter/zkStrata/actions?query=workflow:"example:+passport") |
| Shipment | [examples/shipment/](examples/shipment/) | [![build](https://github.com/MarcKloter/zkStrata/workflows/example%3A%20shipment/badge.svg)](https://github.com/MarcKloter/zkStrata/actions?query=workflow:"example:+shipment") |

## Playground
For a quick hands-on of zkStrata, there is the [zkStrata Playground](https://github.com/MarcKloter/zkStrata-playground) available, which can be launched as a docker container to get a web interface that provides access to a limited set of the core features of zkStrata.

![zkStrata-playground](https://github.com/MarcKloter/zkStrata-playground/blob/master/zkstrata-playground.png?raw=true)

## Executing statements written in zkStrata
To generate and verify zero-knowledge proofs from statements written in zkStrata, we compile source code into an intermediate representation of R1CS gadgets (called [bulletproofs_gadgets](https://github.com/MarcKloter/bulletproofs_gadgets)). This representation can be executed using the experimental R1CS API of [dalek-cryptography's implementation of Bulletproofs](https://github.com/dalek-cryptography/bulletproofs).

## Command-Line Interface
We provide a command-line interface to compile statements written in zkStrata into an executable representation of R1CS gadgets.

The compiler can be built as an executable JAR `target/zkstratac.jar` using:

```
mvn package
```

For usage information check:

```
java -jar zkstratac.jar --help
```

Or have a look at the execution instructions within our [examples](https://github.com/MarcKloter/zkStrata#examples).

## Language Reference
### Statement Syntax
A statement consists of a list of subjects along with a concatenation of predicates expressing claims about them:

```
PROOF FOR subjects THAT predicates
```

**Note:** Keywords in zkStrata are case-insensitive (e.g. `pRoOf FoR` ).

### Subjects Syntax
Subjects are defined as a tuple consisting of an alias for use within predicates and an identifier of a schema describing the data structure of the subject flagged either as `WITNESS` or `INSTANCE` to indicate data confidentiality (private/public). Multiple subject definitions can be linked using `AND`:

```
    WITNESS alias_name COMPLIANT TO schema_name
AND
    INSTANCE alias_name COMPLIANT TO schema_name
```

**Example:**
```
PROOF FOR
    WITNESS myPassport COMPLIANT TO passport_ch
AND
    INSTANCE swissPopulation COMPLIANT TO census_ch
THAT
    ...
```

**Note:**
- `alias_name` and `schema_name` are case-sensitive identifiers (e.g. `myPassport`) matching `[a-zA-Z]([a-zA-Z0-9_])*`.

- Excess newlines and spaces are being ignored and only serve for the purpose of formatting.

### Schemas
A `schema_name` (e.g. `passport_ch`) used as part of a subject declaration will be resolved by the compiler using schemas provided to the CLI and well-known schemas embedded within the compiler. Schemas can be dynamically defined as [JSON Schema](https://json-schema.org/) and passed to the compiler (using the `--schemas` option), see our [examples](https://github.com/MarcKloter/zkStrata#examples). Available well-known schemas can be found within [schemas.predefined](https://github.com/MarcKloter/zkStrata/tree/master/src/main/java/zkstrata/domain/data/schemas/predefined) as `@Schema` annotated classes

Apart from declaring the fields along with data types available to a subject, schemas are allowed to define what we call _validation rules_. A validation rule is a statement expressed in zkStrata relative to subjects compliant to the schema it is defined. Validation rules can be used to declare logic that applies to any subjects of the same structure. We could utilize the validation rule of a passport schema to specify the steps necessary to validate such which is then added to every statement involving a passport. 

**Example:**
```
PROOF FOR
    THIS
THAT
    public.rootHash IS MERKLE ROOT OF (private.firstname, private.lastName)
```

**Note**:
- `THIS` is a special keyword used to declare subjects within validation rules relative to the subject under validation.

- `public` and `private` are reserved aliases by the `THIS` keyword for use within validation rules to access values of the subject under validation.

### Predicates Syntax
Predicates express claims about values of subjects linked using conjunctions. Available conjunctions are logical `AND` and `OR` with the following predicates:

<table>
  <tr>
    <th rowspan="2">Equality</th>
    <th>Description</th>
    <td>Claims equality between two values.</td>
  </tr>
  <tr>
    <th>Syntax</th>
    <td><code>variable = variable</code><br><code>variable IS EQUAL TO variable</code></td>
  </tr>
  <tr>
    <th rowspan="2">Inequality</th>
    <th>Description</th>
    <td>Claims inequality between two values.</td>
  </tr>
  <tr>
    <th>Syntax</th>
    <td><code>variable != variable</code><br><code>variable IS UNEQUAL TO variable</code></td>
  </tr>
  <tr>
    <th rowspan="2">Comparison</th>
    <th>Description</th>
    <td>Claims that the first number is less or greater than the second number.</td>
  </tr>
  <tr>
    <th>Syntax</th>
    <td><code>variable < variable</code><br><code>variable IS LESS THAN variable</code><br><code>variable <= variable</code><br><code>variable IS LESS THAN OR EQUAL TO variable</code><br><code>variable > variable</code><br><code>variable IS GREATER THAN variable</code><br><code>variable >= variable</code><br><code>variable IS GREATER THAN OR EQUAL TO variable</code></td>
  </tr>
  <tr>
    <th rowspan="2">Preimage</th>
    <th>Description</th>
    <td>Claims that a witness value is the preimage of a <a href="https://eprint.iacr.org/2016/492.pdf">MiMCHash-256b</a>.</td>
  </tr>
  <tr>
    <th>Syntax</th>
    <td><code>witness_variable IS PREIMAGE OF variable</code></td>
  </tr>
  <tr>
    <th rowspan="3">Merkle Tree</th>
    <th>Description</th>
    <td>Claims that the given variable is the root hash of a specified merkle tree.</td>
  </tr>
  <tr>
    <th>Syntax</th>
    <td><code>variable IS MERKLE TREE ROOT OF tree</code></td>
  </tr>
  <tr>
    <th>Example</th>
    <td><pre>public.rootHash IS MERKLE TREE ROOT OF (
  (private.firstName, private.lastName),
  (private.dateOfBirth, private.dateOfExpiry)
)</pre></td>
  </tr>
  <tr>
    <th rowspan="3">Set Membership</th>
    <th>Description</th>
    <td>Claims that the given variable is member of the specified set.</td>
  </tr>
  <tr>
    <th>Syntax</th>
    <td><code>variable IS MEMBER OF set</code></td>
  </tr>
  <tr>
    <th>Example</th>
    <td><code>myLicense.category IS MEMBER OF ('A', 'B', 'C')</code></td>
  </tr>
</table>

### Variables Syntax
Variables are used to represent values within predicates either as literal or by referencing fields of subjects. The data types supported are Strings (e.g. `'Peggy'`), unsigned 64-bit integers (e.g. `4037`), bytes in hexadecimal representation (e.g. `0x4b28c209`) and booleans (e.g. `true`). References are composed of an `alias_name` and a series selectors: 

```
alias_name.field_selector[.field_selector]
```

**Note:** References are case-sensitive dot-separated identifiers each matching the pattern `[a-zA-Z]([a-zA-Z0-9_])*`.

**Example:** `myPassport.dateOfBirth.month`

The grammar of predicates declares keywords and constraints on variable confidentiality by using: 

| Name | Description 
| ---- | --------- | 
| `variable` | Both public and private values allowed. |
| `witness_variable` | Only private values by referencing `WITNESS` flagged subjects permitted. |
| `instance_variable` | Only public values through references to `INSTANCE` flagged subjects and literals accepted. |

### Constants
Alternative to using literals as instance data, zkStrata provides constants that are resolved to public values at compile time:

| Name | Description 
| ---- | --------- | 
| `_CURRENT_DATE` | Current date in the format `yyyymmdd`. Example: `20200131` |
| `_CURRENT_DAY_OF_MONTH` | Numeric representation of the current day of the month. Example: `1` to `31` |
| `_CURRENT_MONTH` | Numeric representation of the current month. Example: `1` to `12` |
| `_CURRENT_YEAR` | 4-digit representation of the current year. Example: `2020` |

### Premises
zkStrata allows statements to be optimized based on previously proven claims which we call _premises_. Premises can be used to extend previously shown facts about a subject while producing the minimal set of gadgets required to express new propositions. Such statements can be provided to the CLI using the `--premises` option.
