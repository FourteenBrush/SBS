package me.fourteendoggo.sbs;

import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.argument.address.Address;

import java.util.function.BiConsumer;

public enum OpCode {
    /* lda <source: operand> */
    LDA(1, (sbs, instruction) -> {
        Operand operand = instruction.operands()[0];
        sbs.setCurrent(operand.getValue());
    }),
    /* lde <array start offset: operand> <index: operand> */
    LDE(2, (sbs, instruction) -> {
        Operand arrayStartOffset = instruction.operands()[0];
        Operand index = instruction.operands()[1];
        System.out.printf("array start offset: %s, index: %s%n", arrayStartOffset, index);
        int arrayElement = MemoryAccess.getArrayElement(arrayStartOffset.getValue(), index.getValue());
        sbs.setCurrent(arrayElement);
    }),
    /* sta <destination offset: operand> <value: operand> */
    STA(2, (sbs, instruction) -> {
        Address destinationOffset = instruction.getAddress(0);
        Operand value = instruction.operands()[1];
        MemoryAccess.putInt(destinationOffset, value.getValue());
    }),
    /* add <destination offset: operand> <value: operand> */
    ADD(1, (sbs, instruction) -> {
        Address destinationOffset = instruction.getAddress(0);
        Operand value = instruction.operands()[1];
        MemoryAccess.putInt(destinationOffset, destinationOffset.getValue() + value.getValue());
    }),
    /* sub <destination: operand> <value: operand> */
    SUB(1, (sbs, instruction) -> {
        Operand value = instruction.operands()[0];
        sbs.setCurrent(sbs.getCurrent() - value.getValue());
    }),
    MUL(1, (sbs, instruction) -> {
        Address destinationOffset = instruction.getAddress(0);
        sbs.setCurrent(sbs.getCurrent() * destinationOffset.getValue());
    }),
    DIV(1, (sbs, instruction) -> { // no floating point division
        Address destinationOffset = instruction.getAddress(0);
        Operand value = instruction.operands()[1];
        MemoryAccess.putInt(destinationOffset, destinationOffset.getValue() / value.getValue());
    }),
    INC(1, ((sbs, instruction) -> {
        Address destinationOffset = instruction.getAddress(0);
        int increment = instruction.operands().length > 1 ? instruction.getConstant(1).getValue() : 1;
        MemoryAccess.putInt(destinationOffset, destinationOffset.getValue() + increment);
    })),
    IN(1, (sbs, instruction) -> {
        Constant mode = instruction.getConstant(0);
    }),
    OUT((sbs, instruction) -> System.out.println(sbs.getCurrent())),
    JMP(1, (sbs, instruction) -> { // unconditional jump
        Constant destinationIp = instruction.getConstant(0);
        sbs.jumpTo(destinationIp.getValue());
    }),
    JPP(1, (sbs, instruction) -> {
        if (sbs.getCurrent() > 0) {
            JMP.exec(sbs, instruction);
        }
    }),
    JPZ(1, (sbs, instruction) -> {
        if (sbs.getCurrent() >= 0) {
            JMP.exec(sbs, instruction);
        }
    }),
    JNE(1, (sbs, instruction) -> {
        if (sbs.getCurrent() < 0) {
            JMP.exec(sbs, instruction);
        }
    }),
    JNZ(1, (sbs, instruction) -> {
        if (sbs.getCurrent() <= 0) {
            JMP.exec(sbs, instruction);
        }
    }),
    HLT((sbs, instruction) -> MemoryAccess.free()),
    PS(0, (sbs, instruction) -> {
        MemoryAccess.printState();
        System.out.println("current: " + sbs.getCurrent());
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
        for (OpCode op : OpCode.values()) {
            if (op.name().equalsIgnoreCase(s)) {
                return op;
            }
        }
        throw new IllegalArgumentException("unknown opcode " + s);
    }
}
