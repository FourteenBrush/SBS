package me.fourteendoggo.sbs.argument.address;

import me.fourteendoggo.sbs.MemoryAccess;

public class IndexedByRegisterAddress extends Address {
    private final Register register;

    public IndexedByRegisterAddress(Register register) {
        super(register.address());
        this.register = register;
    }

    @Override
    public int getValue() {
        int offsetWithingRegister = register.getValue();
        return MemoryAccess.getInt(offsetWithingRegister);
    }
}
