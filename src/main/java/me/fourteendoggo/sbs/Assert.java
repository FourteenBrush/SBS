package me.fourteendoggo.sbs;

public interface Assert {

    static void isFalse(boolean condition, String message, Object... args) {
        if (condition) {
            throw new IllegalStateException(message.formatted(args));
        }
    }

    static void isTrue(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalStateException(message.formatted(args));
        }
    }

    static void notNull(Object o, String message, Object... args) {
        if (o == null) {
            throw new IllegalStateException(message.formatted(args));
        }
    }
}
