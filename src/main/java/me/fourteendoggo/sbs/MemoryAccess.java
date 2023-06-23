package me.fourteendoggo.sbs;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.fourteendoggo.sbs.argument.address.Address;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class MemoryAccess {
    private static final boolean ALLOW_UNINITIALIZED_READS = true;
    private static final Unsafe UNSAFE;
    private static final long BASE_ADDRESS;
    private static final LongSet writtenToMemory = new LongArraySet(50);

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe) unsafeField.get(null);
            BASE_ADDRESS = UNSAFE.allocateMemory(64);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static int getInt(Address addressOffset) {
        return getInt(addressOffset.address());
    }

    public static int getInt(int addressOffset) {
        if (!ALLOW_UNINITIALIZED_READS && !writtenToMemory.contains(addressOffset)) {
            throw SBS.panic("cannot access uninitialized memory at address offset %s", addressOffset);
        }
        return UNSAFE.getInt(BASE_ADDRESS + addressOffset);
    }

    public static int getArrayElement(int arrayStartAddressOffset, int index) {
        int offset = arrayStartAddressOffset + index * Integer.BYTES;
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
        int offset = arrayStartAddressOffset + index * Integer.BYTES;
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
        System.out.printf("allow uninitialized reads: %b%n", ALLOW_UNINITIALIZED_READS);
        System.out.printf("base address: 0x%x%n", BASE_ADDRESS);
        System.out.printf("written memory offsets: %s%n", writtenToMemory);
    }
}
