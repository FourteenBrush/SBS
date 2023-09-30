# SBS
Low level language, based on Java bytecode/ assembly, written in Java.  
This project contains a parser and emulator to run the code.

> [!WARNING]
> Work in progress, use at your own risk.

Example file: code.xf  
Run with `java -jar thisjar.jar --input code.xf`

## Syntax

Example:

```asm
; equivalent of IntStream.range(1, 4).forEach(System.out::println);

@define max 4

     mov $ax 1       ; i
lp:  mov $cx max
     sub $cx $ax     ; max - i
     jnz $cx hlt
     ; loop body
     out $ax
     inc $ax         ; i++
     pd              ; added this so you can see what's going on internally (print diagnostics)

     jmp lp
hlt: hlt
```

### Preprocessor directives

A file starts with preprocessor directives (`@define foo bar`), similar to how C handles such directives.
These will be replaced in the source code before parsing, e.g. every occurrence of `foo` will be replaced with `bar`.
This can be useful to avoid magic values.  
> [!NOTE]
> More directives will come in future releases.

Example:

```asm
@define ON 1
@define OFF 0

@define bool_addr 128
mov [bool_addr] ON

; or with address notation
@define bool_addr [128]
mov bool_addr ON
```

### Markers

Every further line is either a marker or an instruction. A marker acts as *yes a marker* to some instruction behind it.
This is useful for jumps, so you write something like `jmp my_marker` instead of magic values, markers will be preprocessed too.  
> [!NOTE]
> When writing a marker, the expression it points to must be on the same line, this will be resolved in a future release.

Example marker:

```asm
exit: hlt ; exit is the marker and hlt is the opcode which halts execution of the program

; program start
mov [0] 100
jp [0] exit ; if 100 is positive, halt program execution
```

Internally, markers decay to constants.

### Instructions

Syntax: `opcode` (see below) `zero or more operands`

Example instructions:

```asm
; move the constant 12 to the ax register
mov $ax 12
```

## Opcodes

| Opcode | Syntax (case insensitive)                            | Description                                            |
|--------|------------------------------------------------------|--------------------------------------------------------|
| `NOP`  | nop                                                  | does nothing                                           |
| `MOV`  | (mov <destination: address> <source: operand>)       | move memory                                            |
| `LDE`  | (lde <array start offset: operand> <index: operand>) | load array element, `subject to deprecation`           |
| `ADD`  | (add <destination: operand> <value: operand>)        | add `value` to `destination`                           |
| `SUB`  | (sub <destination: address> <value: operand>)        | similar                                                |
| `MUL`  | (mul <destination: address> <value: operand>)        | similar                                                |
| `DIV`  | (div <destination: address> <value: operand>)        | similar                                                |
| `INC`  | (inc <destination: address> \[value: operand \| 1])  | increase the inner value of `destination` by one       |
| `IN`   | in <mode: constant>                                  | todo                                                   |
| `OUT`  | out <what: operand>                                  | prints to stdout                                       |
| `JMP`  | jmp <destination: address>                           | jumps to an address and continues execution from there |
| `JP`   | jp <value: operand> <destination: address>           | jump if positive, similar                              |
| `JPZ`  | jpz <value: operand> <destination: address>          | jump is positive or zero                               |
| `JNE`  | jne <value: operand> <destination: address>          | jump if negative                                       |
| `JNZ`  | jnz <value: operand> <destination: address>          | jump if negative or zero                               |
| `JZ`   | jz <value: operand> <destination: address>           | jump if zero                                           |
| `HLT`  | hlt                                                  | stops execution, `must be called to cleanup and exit`  |
| `PD`   | pd                                                   | prints diagnostic info on the stdout                   |

## Operands

All operands are currently `32-bit`

> [!NOTE]
> Floating point numbers are not supported yet.

Operands come in three forms:
- immediate value (constant) e.g. `2`
- address e.g. `[124]`
- register, e.g. `$ax`, currently from `ax` to `dx`, always preceded by `$`

Operand  
├─ Constant  
├─ Register  
├─ Address  
(operand means really anything can be given as argument)

Operands can be combined to make more complicated operands:
- `[$bc + 2]` array element access, $bx contains the base address of the array and we read the second element
- `[$cx + [addr_directive]]` uhh

## Todo

- [] Boolean related instructions (and, or, etc.)
- [] Bitshift instructions
- [] Implementing strings
- [] Implementing function calls, probably with some custom calling convention
- [] Implementing floating point values
- [] Syscall interface maybe?