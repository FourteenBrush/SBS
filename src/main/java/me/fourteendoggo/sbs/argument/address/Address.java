package me.fourteendoggo.sbs.argument.address;

import me.fourteendoggo.sbs.MemoryAccess;
import me.fourteendoggo.sbs.argument.Operand;

/**
 * An address is a pointer to a memory location. Currently, there are three addressing modes:
 * <ul>
 *     <li>Direct addressing: [1000]</li>
 *     <li>Indirect addressing: [$ax]</li>
 *     <li>Indexed addressing: [$ax + 10]</li>
 * </ul>
 */
public abstract class Address implements Operand {
    private final int address;

    public Address(int address) {
        this.address = address;
    }

    @Override
    public int getValue() {
        return MemoryAccess.getInt(address());
    }

    public int address() {
        return address;
    }

    @Override
    public String toString() {
        return "%s[address=%s]".formatted(getClass().getSimpleName(), address);
    }
}
