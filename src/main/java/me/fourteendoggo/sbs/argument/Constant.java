package me.fourteendoggo.sbs.argument;

// avoid having both a getValue() and value() by calling the parameter getValue
public record Constant(int getValue) implements Operand {

    @Override
    public String toString() {
        return "Constant[value=" + getValue + "]";
    }
}
