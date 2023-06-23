package me.fourteendoggo.sbs.instruction;

import me.fourteendoggo.sbs.MemoryAccess;
import me.fourteendoggo.sbs.RegisterHolder;
import me.fourteendoggo.sbs.SBS;
import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.argument.address.Address;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public enum OpCode {
    NOP(0, (sbs, instruction) -> {}),
    /** mov <destination: address> <source: operand> */
    MOV(2, (sbs, instruction) -> {
       Address destination = instruction.getAddress(0);
       Operand source = instruction.operands()[1];
       MemoryAccess.putInt(destination, source.getValue());
    }),
    /** lde <array start offset: operand> <index: operand> */
    LDE(2, (sbs, instruction) -> {
        Operand arrayStartOffset = instruction.operands()[0];
        Operand index = instruction.operands()[1];
        int arrayElement = MemoryAccess.getArrayElement(arrayStartOffset.getValue(), index.getValue());
        MemoryAccess.putInt(RegisterHolder.getOrThrow("ax"), arrayElement);
    }),
    /** add <destination offset: operand> <value: operand> */
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
    INC(1, ((sbs, instruction) -> {
        Address destinationOffset = instruction.getAddress(0);
        MemoryAccess.putInt(destinationOffset, destinationOffset.getValue() + 1);
    })),
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
        sbs.setIp(destinationIp.getValue());
    }),
    /** jp <value: operand> <destination: address> */
    JP(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() > 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.setIp(destinationIp.getValue());
        }
    }),
    JPZ(2, ((sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() >= 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.setIp(destinationIp.getValue());
        }
    })),
    JNE(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() < 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.setIp(destinationIp.getValue());
        }
    }),
    JNZ(2, (sbs, instruction) -> {
        Operand toCheck = instruction.operands()[0];
        if (toCheck.getValue() <= 0) {
            Constant destinationIp = instruction.getConstant(1);
            sbs.setIp(destinationIp.getValue());
        }
    }),
    HLT((sbs, instruction) -> {
        MemoryAccess.free();
        sbs.halt(0);
    }),
    PD(0, (sbs, instruction) -> {
        System.out.printf("%s Diagnostic state %s%n", "=".repeat(6), "=".repeat(7));
        MemoryAccess.printState();
        RegisterHolder.printDiagnostics();
        System.out.println("=".repeat(30));
    });

    private final int requiredArgs;
    private final BiConsumer<SBS, Instruction> function;

    OpCode(BiConsumer<SBS, Instruction> function) {
        this(0, function);
    }

    OpCode(int requiredArgs, BiConsumer<SBS, Instruction> function) {
        this.requiredArgs = requiredArgs;
        this.function = function;
    }

    public int getRequiredArgs() {
        return requiredArgs;
    }

    public void exec(SBS sbs, Instruction instruction) {
        function.accept(sbs, instruction);
    }

    public static OpCode fromString(String s) {
        return valueOf(s.toUpperCase()); // TODO: let caller do this themselves
    }
}
