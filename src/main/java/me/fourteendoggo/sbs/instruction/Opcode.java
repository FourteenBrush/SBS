package me.fourteendoggo.sbs.instruction;

import me.fourteendoggo.sbs.MemoryAccess;
import me.fourteendoggo.sbs.RegisterHolder;
import me.fourteendoggo.sbs.SBS;
import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.argument.address.Address;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public enum Opcode {
    NOP(0, (sbs, instruction) -> {}),
    /** mov <destination: address> <source: operand> */
    MOV(2, (sbs, instruction) -> {
       Address destination = instruction.getAddress(0);
       Operand source = instruction.operands()[1];
       MemoryAccess.putInt(destination, source.getValue());
    }),
    /** lde <array start offset: operand> <index: operand> */
    @Deprecated
    LDE(2, (sbs, instruction) -> {
        Operand arrayStartOffset = instruction.operands()[0];
        Operand index = instruction.operands()[1];
        int arrayElement = MemoryAccess.getArrayElement(arrayStartOffset.getValue(), index.getValue());
        MemoryAccess.putInt(RegisterHolder.getOrThrow("ax"), arrayElement); // what?
    }),
    /** add <destination: operand> <value: operand> */
    ADD(1, (sbs, instruction) -> {
        Address destinationOffset = instruction.getAddress(0);
        Operand value = instruction.operands()[1];
        MemoryAccess.putInt(destinationOffset, destinationOffset.getValue() + value.getValue());
    }),
    /** sub <destination: address> <value: operand> */
    SUB(2, (sbs, instruction) -> {
       Address destination = instruction.getAddress(0);
       Operand toSubtract = instruction.operands()[1];
       MemoryAccess.putInt(destination, destination.getValue() - toSubtract.getValue());
    }),
    /** mul <destination: address> <value: operand> */
    MUL(2, (sbs, instruction) -> {
        Address destination = instruction.getAddress(0);
        Operand multiplier = instruction.operands()[1];
        MemoryAccess.putInt(destination, destination.getValue() * multiplier.getValue());
    }),
    /** div <destination: address> <value: operand> */
    DIV(2, (sbs, instruction) -> { // no floating point division
        Address destination = instruction.getAddress(0);
        Operand divisor = instruction.operands()[1];
        MemoryAccess.putInt(destination, destination.getValue() / divisor.getValue());
    }),
    /** inc <destination: address> [value: operand] */
    INC(2, (sbs, instruction) -> { // TODO 1-2 operands
        Address destinationOffset = instruction.getAddress(0);
        int value = instruction.operands().length > 1 ? instruction.operands()[1].getValue() : 1;
        MemoryAccess.putInt(destinationOffset, destinationOffset.getValue() + value);
    }),
    IN(1, (sbs, instruction) -> {
        Constant mode = instruction.getConstant(0);
        System.out.println("Input from mode " + mode.getValue());
        // TODO
    }),
    /** out <what: operand> */
    OUT(1, (sbs, instruction) -> {
        Operand what = instruction.operands()[0];
        System.out.println("[OUT] " + what.getValue());
    }),
    /** jmp <destination: address> */
    JMP(1, (sbs, instruction) -> {
        Constant destinationIp = instruction.getConstant(0);
        sbs.jumpTo(destinationIp.getValue());
    }),
    /** jp <value: operand> <destination: address> */
    JP(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() > 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.jumpTo(destinationIp.getValue());
        }
    }),
    /** jpz <value: operand> <destination: address> */
    JPZ(2, ((sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() >= 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.jumpTo(destinationIp.getValue());
        }
    })),
    /** jne <value: operand> <destination: address> */
    JNE(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() < 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.jumpTo(destinationIp.getValue());
        }
    }),
    /** jnz <value: operand> <destination: address> */
    JNZ(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() <= 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.jumpTo(destinationIp.getValue());
        }
    }),
    /** jz <value: operand> <destination: address> */
    JZ(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() == 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.jumpTo(destinationIp.getValue());
        }
    }),
    HLT((sbs, instruction) -> {
        MemoryAccess.free();
        sbs.halt(0);
    }),
    PD(0, (sbs, instruction) -> {
        System.out.printf("%s Diagnostics %s%n", "=".repeat(6), "=".repeat(7));
        MemoryAccess.printState();
        RegisterHolder.printDiagnostics();
        System.out.println("=".repeat(30));
    });

    private final int minArgs, maxArgs;
    private final BiConsumer<SBS, Instruction> function;

    Opcode(BiConsumer<SBS, Instruction> function) {
        this(0, function);
    }

    Opcode(int requiredArgs, BiConsumer<SBS, Instruction> function) {
        this(requiredArgs, requiredArgs, function);
    }

    Opcode(int minArgs, int maxArgs, BiConsumer<SBS, Instruction> function) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.function = function;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public int getMaxArgs() {
        return maxArgs;
    }

    public void execute(SBS sbs, Instruction instruction) {
        function.accept(sbs, instruction);
    }

    public static Opcode fromString(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No such opcode " + s);
        }
    }
}
