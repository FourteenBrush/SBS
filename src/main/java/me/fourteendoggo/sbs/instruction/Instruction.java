package me.fourteendoggo.sbs.instruction;

import me.fourteendoggo.sbs.SBS;
import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.argument.address.Address;

import java.util.Arrays;

public record Instruction(Opcode opcode, Operand... operands) {

    public void execute(SBS sbs) {
        opcode.execute(sbs, this);
    }

    public Constant getConstant(int idx) {
        return get(idx, Constant.class);
    }

    public Address getAddress(int idx) {
        return get(idx, Address.class);
    }

    @SuppressWarnings("unchecked") // stupid analysis tool
    private <T extends Operand> T get(int idx, Class<T> expectedType) {
        Operand operand = operands[idx];
        if (!expectedType.isInstance(operand)) {
            throw new IllegalArgumentException("operand at index %s is not of type %s, got %s".formatted(idx, expectedType, operand.getClass()));
        }
        return (T) operand;
    }

    @Override
    public String toString() {
        return "Instruction[opcode=%s, operands=%s]".formatted(
                opcode, Arrays.toString(operands)
        );
    }
}
