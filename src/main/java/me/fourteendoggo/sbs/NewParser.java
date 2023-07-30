package me.fourteendoggo.sbs;

/*
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.instruction.Instruction;
import me.fourteendoggo.sbs.instruction.OpCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewParser {
    private final List<Instruction> instructions = new ArrayList<>(); // need O(1) access time when executing
    private final Map<String, Operand> directives = new HashMap<>();
    private final Object2IntMap<String> markers = new Object2IntArrayMap<>(20);
    private final List<UnresolvedMarker> unresolvedMarkers = new ArrayList<>();

    public List<Instruction> parseInstructions(String code) {
        List<Instruction> instructions = new ArrayList<>();
        String[] lines = code.split("\n");

        int instructionPtr = 0;
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum].trim();
            if (line.isEmpty()) continue;

            int commentIdx = line.indexOf(';');
            if (commentIdx != -1) {
                line = line.substring(0, commentIdx);
                if (line.isEmpty()) continue; // whole line comment
            }
            String[] splitInstruction = splitLine(line);

            // dont increment instructionPtr for preprocessor directives
            if (line.charAt(0) == '@') {
                parseDirective(splitInstruction);
                continue;
            }
            instructions.add(parseInstruction(lineNum, instructionPtr++, splitInstruction));
        }
        return instructions;
    }

    // to avoid using Pattern#split
    private String[] splitLine(String line) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        int end = 0;
        boolean inBrackets = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '[') {
                inBrackets = true;
            } else if (c == ']') {
                inBrackets = false;
            } else if (!inBrackets && Character.isWhitespace(c)) {
                // found a whitespace character outside brackets
                if (start != end) { // ignore consecutive whitespace
                    parts.add(line.substring(start, end));
                }
                start = i + 1; // start the next part after this whitespace
                end = start;
                continue; // don't add the whitespace character to the current part
            }
            end++;
        }
        if (start != end) { // add the last part
            parts.add(line.substring(start, end));
        }
        return parts.toArray(String[]::new);
    }

    private void parseDirective(String[] splitInstruction) {
        // currently only account for "@define"
        Assert.isTrue(splitInstruction[0].equalsIgnoreCase("@define"), "TRAP");
        Assert.isTrue(splitInstruction.length >= 3, "invalid directive format: must be @action name replacement");
        String directiveName = splitInstruction[1];
        String replacement = splitInstruction[2];
        // currently only supports one word as replacement
        directives.put(directiveName, parseOperand(replacement));
        System.out.printf("parsed directive %s: %s%n", directiveName, replacement);
    }

    private Instruction parseInstruction(int lineNum, int instructionPtr, String[] splitInstruction) {
        int opcodeIdx = 0;
        String possibleMarker = splitInstruction[0];
        if (possibleMarker.endsWith(":")) {
            String marker = possibleMarker.substring(0, possibleMarker.length() - 1); // remove ':'
            Assert.isFalse(markers.containsKey(marker), "line %s: cannot define marker '%s' twice", marker);
            markers.put(marker, instructionPtr);
            opcodeIdx = 1;
        }
        Assert.isFalse(opcodeIdx >= splitInstruction.length, "line %s: expected an opcode after marker", lineNum);
        OpCode opcode = OpCode.fromString(splitInstruction[opcodeIdx]);
        int operandCount = splitInstruction.length - opcodeIdx - 1;
        Assert.isFalse(
                operandCount != opcode.getRequiredArgs(),
                "line %s: expected %s operands for opcode %s but got %s",
                lineNum, opcode.getRequiredArgs(), opcode, operandCount
        );
        Operand[] operands = new Operand[operandCount];
        // parse operands
        for (int i = 0; i < operands.length; i++) {
            String operandStr = splitInstruction[opcodeIdx + i + 1];
            Operand operand = parseOperand(operandStr);
            if (operand == null) {
                UnresolvedMarker unresolvedMarker = new UnresolvedMarker(operandStr, operands, i);
                unresolvedMarkers.add(unresolvedMarker);
                continue;
            }
            operands[i] = operand;
        }
        return new Instruction(opcode, operands);
    }

    private Operand parseOperand(String str) {
        char firstChar = str.charAt(0);
        if (isLowercaseLetter(firstChar)) {
            return resolveMarker();
        } else if (firstChar == '0') {
            int value = str.startsWith("0x")
                    ? Integer.parseInt(str.substring(2), 16)
                    : Integer.parseInt(str);
            return new Constant(value);
        } else if (isNumerical(firstChar)) {
            return new Constant(Integer.parseInt(str));
        }
        throw new IllegalArgumentException("malformed operand '" + str + "'");
    }

    private boolean isLowercaseLetter(char c) {
        return c >= 'a' && c <= 'z';
    }

    private boolean isNumerical(char c) {
        return c >= '0' && c <= '9';
    }

    private record UnresolvedMarker(String markerName, Operand[] operands, int operandIdx) {}
}
*/