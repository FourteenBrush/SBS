# SBS
Low level language, based on Java bytecode/ assembly, written in Java.  
This project contains a parser and emulator.

## Syntax

Example:

```asm
; equivalent of IntStream.range(1, 4).forEach(System.out::println);

@define max_ 4

     mov $ax 1       ; i
lp:  mov $cx max_
     sub $cx $ax     ; max - i
     jnz $cx hlt
     ; loop body
     out $ax
     inc $ax         ; i++
     pd              ; added this so you can see what's going on internally (print diagnostics)

     jmp lp
hlt: hlt
```

A file starts with preprocessor directives (`@define foo bar`), similar to how C handles such directives.
These will be replaces in the source code before parsing, e.g. `foo` will be replaced with `bar` on all lines.
This is useful to avoid magic constants.

Every further line is either a marker or an instruction. A marker acts as *yes a marker* to some instruction behind it.
Useful for jumps so you write something like `jmp my_marker` instead of magic values, these will be preprocessed too.

Example marker:

```asm
exit: hlt ; exit is the marker and hlt is the opcode which halts execution
```

Example instruction:

```asm
; puts the constant 12 in register ax
mov $ax 12
```

Synytax: `opcode` (see below) `zero or more operands`

## Opcodes

*have i never heard of a table hmmm*

Operand  
├─ Constant  
├─ Register  
├─ Address  
(operand means really anything can be passed in)

`NOP`: no-op  
`MOV`: (mov <destination: address> <source: operand>)  - move memory  
`LDE`: (lde <array start offset: operand> <index: operand>) - load array element, subject to deprecation  
`ADD`: (add <destination: operand> <value: operand>)  - add `value` to `destination`  
`SUB` (sub <destination: address> <value: operand>)  - similar  
`MUL` (mul <destination: address> <value: operand>)  - similar  
`DIV` (div <destination: address> <value: operand>)  - similar  
`INC` (inc <destination: address>)  - increase the inner value of `destination` by one, todo add custom amount  
`IN` () - reads input, todo  
`OUT` (out <what: operand>) - prints to stdout  
`JMP` (jmp <destination: address>) - jumps to an address and continues execution from there  
`JP` (jp <value: operand> <destination: address>) - jump if positive, similar  
`JPZ` (jpz <value: operand> <destination: address>) - jump is positive or zero  
`JNE` (jne <value: operand> <destination: address>) - jump if negative  
`JNZ` (jnz <value: operand> <destination: address>) - jump if negative or zero  
`HLT` () - stops execution, `must be called to cleanup and exit`  
`PD` () - dumps some diagnostic stuff on the stdout  

## Operands

Operands are currently `32-bit`

Operands come in three forms:
- immediate value (constant) `e.g. 2`
- address e.g. `[124]`
- register, e.g. `$ax`, currently from `ax` to `dx`, always preceded by `$`

Operands can be combined to make more complicated operands:
- `[$bc + 2]` array access, $bx contains the start address of an array and we read the 
- `[$cx + [addr_directive]]` uhhh
