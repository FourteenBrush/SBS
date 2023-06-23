package me.fourteendoggo.sbs;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.fourteendoggo.sbs.argument.Constant;
import me.fourteendoggo.sbs.argument.Operand;
import me.fourteendoggo.sbs.argument.address.Address;
import me.fourteendoggo.sbs.argument.address.DirectAddress;
import me.fourteendoggo.sbs.instruction.Instruction;
import me.fourteendoggo.sbs.instruction.OpCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private static final int UNDEFINED_MARKER_IP = -999;
    private final Map<String, Operand> directives = new HashMap<>();
    private final Object2IntMap<String> markers = new Object2IntArrayMap<>(5);
    private final List<UnresolvedMarker> unresolvedMarkers = new ArrayList<>();

    public Parser() {
        markers.defaultReturnValue(UNDEFINED_MARKER_IP);
        RegisterHolder.createRegister("ax");
        RegisterHolder.createRegister("bx");
        RegisterHolder.createRegister("cx");
        RegisterHolder.createRegister("dx");
    }

    public List<Instruction> parseInstructions(List<String> codeLines) {
        List<Instruction> instructions = new ArrayList<>(codeLines.size());

        int instructionPtr = 0;
        for (int lineNum = 0; lineNum < codeLines.size(); lineNum++) {
            String line = codeLines.get(lineNum).trim();
            if (line.isEmpty()) continue;

            // remove comments first because they might appear in directives too
            int commentIdx = line.indexOf(';');
            if (commentIdx != -1) {
                line = line.substring(0, commentIdx);
                if (line.isEmpty()) continue; // whole line comment
            }

            // dont increment instructionPtr for directives
            if (line.startsWith("@define")) {
                parseDirective(line);
                continue;
            }
            String[] splitInstruction = splitLine(line);

            Instruction instruction = parseInstruction(lineNum, instructionPtr++, splitInstruction);
            instructions.add(instruction);
        }
        // second pass - resolve all markers we were unable to in the first pass
        // because we read them but their destination instruction ptr was not yet seen in the source code
        unresolvedMarkers.removeIf(marker -> {
            marker.resolve(markers);
            return true;
        });
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

    /*
    A directive is a line such as "@define foo 5" which defines an operand named "foo" with value 5.
    That constant can then be used in the program as an operand instead of specifying a magic number.
    This is converted to the actual value when the program is parsed.
     */
    private void parseDirective(String line) {
        int emptyCharIdx = line.lastIndexOf(' ');
        String directiveName = line.substring("@define ".length(), emptyCharIdx);
        String operandStr = line.substring(emptyCharIdx + 1);
        Operand replacement = parseOperand(operandStr);
        directives.put(directiveName, replacement);

        System.out.printf("parsed directive %s: %s%n", directiveName, replacement);
    }

    private Instruction parseInstruction(int lineNum, int instructionPtr, String[] splitInstruction) {
        int opcodeIdx = 0;
        String possibleMarker = splitInstruction[0];
        if (possibleMarker.endsWith(":")) {
            String marker = possibleMarker.substring(0, possibleMarker.length() - 1); // remove ':'
            Assert.isFalse(markers.containsKey(marker), "line %s: cannot define marker '%s' twice", lineNum, marker);
            markers.put(marker, instructionPtr);
            opcodeIdx = 1; // we have a marker first, then an opcode
        }
        Assert.isFalse(opcodeIdx >= splitInstruction.length, "line %s: expected an opcode after marker", lineNum);

        OpCode opCode = OpCode.fromString(splitInstruction[opcodeIdx]);
        int operandCount = splitInstruction.length - opcodeIdx - 1;
        Assert.isFalse(
                operandCount != opCode.getRequiredArgs(),
                "line %s: expected %s operands for opcode %s but got %s",
                lineNum, opCode.getRequiredArgs(), opCode, operandCount
        );
        Operand[] operands = new Operand[operandCount];

        for (int i = 0; i < operands.length; i++) {
            String operandStr = splitInstruction[opcodeIdx + i + 1];
            Operand operand = parseOperand(operandStr);
            if (operand == null) {
                // we received null, meaning the operand was a marker which could not be read this early
                unresolvedMarkers.add(new UnresolvedMarker(operandStr, operands, i));
                continue;
            }
            operands[i] = operand;
        }
        return new Instruction(opCode, operands);
    }

    private Operand parseOperand(String str) {
        return switch (str.charAt(0)) { // example 1234 - constant
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                int value = Integer.parseInt(str);
                yield new Constant(value);
            }
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> resolveDirectiveOrMarker(str);
            case '$' -> { // example $a - register value
                String registerName = str.substring(1);
                Assert.isTrue(str.length() > 1, "expected a register name behind a $");
                yield RegisterHolder.getOrThrow(registerName);
            }
            case '[' -> { // example [$ax] or [100] or [$ax + 100]
                Assert.isTrue(str.length() > 2, "cannot parse an empty []");
                Assert.isTrue(str.charAt(str.length() - 1) == ']', "expected closing ]");
                yield parseWithinSquareBrackets(str);
            }
            default -> throw new IllegalArgumentException("cannot parse operand '" + str + "'");
        };
    }

    private Operand resolveDirectiveOrMarker(String str) {
        // we must check directives first, because a marker not being present in the map could either mean:
        // the marker just doesn't exist
        // we haven't encountered that marker definition yet in the source code, but we are still going to
        Operand directive = directives.get(str);
        if (directive != null) {
            return directive;
        }
        int markerDestination = markers.getInt(str);
        if (markerDestination != UNDEFINED_MARKER_IP) {
            return new Constant(markerDestination);
        }
        return null; // assume this is a marker which hasn't been read yet - will be replaced in the second pass
    }

    /*
    between square brackets, there may be a combination of operands, examples:
    opcode [$ax + 1000] (register + constant)
    opcode [$ax + $bx] (register + register)
    opcode [$ax + directive_a] (register + directive pointing to constant)
     */
    private Operand parseWithinSquareBrackets(String str) {
        String[] operands = str.substring(1, str.length() - 1).split("\\+"); // takes String#format fast path

        int finalOffset = 0;
        for (String operandStr : operands) {
            Operand operand = parseOperand(operandStr.trim());
            switch (operand) {
                case null -> throw new IllegalArgumentException("cannot parse operand " + operandStr);
                case Constant constant -> finalOffset += constant.getValue();
                case Address address -> finalOffset += address.address();
                default -> throw new AssertionError("default switch case should not be triggered");
            }
        }
        return new DirectAddress(finalOffset);
    }

    private record UnresolvedMarker(String markerName, Operand[] operands, int operandIdx) {

        public void resolve(Object2IntMap<String> markers) {
            int destinationInstructionPtr = markers.getOrDefault(markerName, UNDEFINED_MARKER_IP);
            Assert.isFalse(destinationInstructionPtr == UNDEFINED_MARKER_IP, "marker %s is not defined", markerName);
            operands[operandIdx] = new Constant(destinationInstructionPtr);
        }
    }
}
