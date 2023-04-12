package me.fourteendoggo.sbs;

import java.util.List;

public class SBS {
    private int ip;
    private int current;
    private final List<Instruction> instructions;

    public SBS(String code) {
        Parser parser = new Parser();
        instructions = parser.parseInstructions(code);
    }

    @SuppressWarnings("ConstantConditions")
    public void exec() {
        if (instructions.isEmpty()) return;

        Instruction instruction = null;
        while (ip < instructions.size()) {
            instruction = instructions.get(ip);
            System.out.println(instruction);

            int oldIp = ip;
            instruction.execute(this);
            if (ip == oldIp) { // no jump
                ip++;
            }
            if (instruction.opCode() == OpCode.HLT) break;
        }
        if (instruction.opCode() != OpCode.HLT) {
            throw error("last instruction was not a HLT, executed it manually and exited");
        }
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void jumpTo(int ip) {
        if (ip < 0 || ip >= instructions.size()) {
            throw new IllegalArgumentException("ip must be between 0 and %s, got %s".formatted(instructions.size(), ip));
        }
        this.ip = ip;
    }

    protected static RuntimeException error(String message, Object... placeholders) {
        MemoryAccess.free();
        return new IllegalStateException(message.formatted(placeholders));
    }
}