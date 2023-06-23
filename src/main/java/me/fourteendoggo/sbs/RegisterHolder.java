package me.fourteendoggo.sbs;

import me.fourteendoggo.sbs.argument.address.Register;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterHolder {
    private static final Map<String, Register> REGISTERS = new HashMap<>();
    // TODO: registers now start at address offset 32, what about users overwriting this?
    // could be solved with placing registers at a completely different place in memory, which is not user accessible
    private static final AtomicInteger OFFSET_INCREMENT = new AtomicInteger(32);

    public static void createRegister(String name) {
        int offset = OFFSET_INCREMENT.getAndAdd(Integer.BYTES);
        REGISTERS.put(name, new Register(offset));
    }

    public static Register getOrThrow(String name) {
        Register register = REGISTERS.get(name);
        Assert.notNull(register, "unknown register %s", name);
        return register;
    }

    public static void printDiagnostics() {
        REGISTERS.forEach((name, register) -> System.out.printf("Register %s (%s): %s%n",
                name, register.address(), register.getValue()));
    }
}
