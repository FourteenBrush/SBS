package me.fourteendoggo.sbs;

import me.fourteendoggo.sbs.instruction.Instruction;
import me.fourteendoggo.sbs.instruction.Opcode;

import java.util.Arrays;
import java.util.List;

public class SBS {
    private int pc;
    private final List<Instruction> instructions;

    public SBS(String code) {
        this(Arrays.asList(code.split("\n")));
    }

    public SBS(List<String> codeLines) {
        Parser parser = new Parser();
        instructions = parser.parseInstructions(codeLines);
    }

    @SuppressWarnings("ConstantConditions")
    public void run() {
        if (instructions.isEmpty()) return;

        Instruction instruction = null;
        while (pc < instructions.size()) {
            instruction = instructions.get(pc);

            int oldIp = pc;
            instruction.execute(this);
            if (pc == oldIp) { // no jump
                pc++;
            }
            if (instruction.opcode() == Opcode.HLT) return;
        }
        if (instruction.opcode() != Opcode.HLT) {
            panic("last instruction was not a HLT, executed it manually and exited");
        }
    }

    public void jumpTo(int ip) {
        if (ip < 0 || ip >= instructions.size()) {
            panic("ip must be between 0 and %s, got %s".formatted(instructions.size(), ip));
        }
        pc = ip;
    }

    public void halt(int status) {
        MemoryAccess.free();
        System.exit(status);
    }

    protected static void panic(String message, Object... placeholders) {
        MemoryAccess.free();
        throw new IllegalStateException(message.formatted(placeholders));
    }
}