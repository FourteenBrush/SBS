package me.fourteendoggo.sbs;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.fourteendoggo.sbs.argument.address.Address;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class MemoryAccess {
    public static final int REGISTER_SIZE = Integer.BYTES;
    private static final boolean ALLOW_UNINITIALIZED_READS;
    private static final Unsafe UNSAFE;
    private static final long BASE_ADDRESS;
    private static final LongSet writtenToMemory = new LongArraySet(50);

    static {
        ALLOW_UNINITIALIZED_READS = Boolean.parseBoolean(System.getProperty("sbs.allow-uninitialized-reads", "true")); // works ig
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe) unsafeField.get(null);
            BASE_ADDRESS = UNSAFE.allocateMemory(256);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Cannot use sun.misc.Unsafe for memory access");
            throw new ExceptionInInitializerError(e);
        }
    }

    public static int getInt(Address addressOffset) {
        return getInt(addressOffset.address());
    }

    public static int getInt(int addressOffset) {
        if (!ALLOW_UNINITIALIZED_READS && !writtenToMemory.contains(addressOffset)) {
            SBS.panic("cannot access uninitialized memory at address offset %s", addressOffset);
        }
        return UNSAFE.getInt(BASE_ADDRESS + addressOffset);
    }

    public static int getArrayElement(int arrayStartAddressOffset, int index) {
        int offset = arrayStartAddressOffset + index * REGISTER_SIZE;
        return getInt(offset);
    }

    public static void putInt(Address addressOffset, int value) {
        putInt(addressOffset.address(), value);
    }

    public static void putInt(int addressOffset, int value) {
        writtenToMemory.add(addressOffset);
        UNSAFE.putInt(BASE_ADDRESS + addressOffset, value);
    }

    public static void putArrayElement(int arrayStartAddressOffset, int index, int value) {
        int offset = arrayStartAddressOffset + index * REGISTER_SIZE;
        putInt(offset, value);
    }

    public static void putArray(int arrayStartAddressOffset, int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            putArrayElement(arrayStartAddressOffset, i, arr[i]);
        }
    }

    public static void free() {
        writtenToMemory.clear();
        UNSAFE.freeMemory(BASE_ADDRESS);
    }

    public static void printState() {
        System.out.printf("allows uninitialized reads: %b%n", ALLOW_UNINITIALIZED_READS);
        System.out.printf("base address: 0x%x%n", BASE_ADDRESS);
        System.out.printf("written memory offsets: %s%n", writtenToMemory);
    }
}
