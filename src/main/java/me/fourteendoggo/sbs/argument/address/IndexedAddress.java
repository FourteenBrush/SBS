package me.fourteendoggo.sbs.argument.address;

import me.fourteendoggo.sbs.MemoryAccess;

public class IndexedAddress extends Address {
    private final Register register;
    private final int offset;

    public IndexedAddress(Register register, int offset) {
        super(register.address());
        this.register = register;
        this.offset = offset;
    }

    @Override
    public int getValue() {
        int absoluteOffset = register.getValue() + offset;
        return MemoryAccess.getInt(absoluteOffset);
    }
}
