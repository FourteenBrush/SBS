package me.fourteendoggo.sbs;

import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.argument.address.Address;

import java.util.Arrays;

public record Instruction(OpCode opCode, Operand... operands) {

    public void execute(SBS sbs) {
        opCode.exec(sbs, this);
    }

    public Constant getConstant(int idx) {
        return get(idx, Constant.class);
    }

    public Address getAddress(int idx) {
        return get(idx, Address.class);
    }

    private <T extends Operand> T get(int idx, Class<T> expectedType) {
        Operand operand = operands[idx];
        if (!expectedType.isInstance(operand)) {
            throw new IllegalArgumentException("operand at index %s is not of %s, got %s".formatted(idx, expectedType, operand.getClass()));
        }
        return expectedType.cast(operand);
    }

    @Override
    public String toString() {
        return "Instruction[opCode=%s, operands=%s]".formatted(
                opCode, Arrays.toString(operands)
        );
    }
}
