/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.MemoryStub;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import net.jcip.annotations.Immutable;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * CPU Verifier.
 * <p>
 * Used for checking the result (registers, flags, memory) after test execution.
 */
@Immutable
public abstract class CpuVerifier {
    /**
     * The memory stub used for reading memory values during verification.
     */
    protected final MemoryStub<? extends Number> memoryStub;

    /**
     * Creates a new CPU verifier with the specified memory stub.
     *
     * @param memoryStub the memory stub to use for reading memory values
     */
    public CpuVerifier(MemoryStub<?> memoryStub) {
        this.memoryStub = Objects.requireNonNull(memoryStub);
    }

    /**
     * Checks that the byte at the specified memory address matches the expected value.
     *
     * @param address the memory address to check
     * @param expected the expected byte value
     * @throws AssertionError if the actual value doesn't match expected
     */
    public void checkMemoryByte(int address, int expected) {
        expected &= 0xFF;
        int actual = memoryStub.read(address).intValue() & 0xFF;
        assertEquals(
                String.format("Expected mem[%04x]=%02x, but was %02x", address, expected, actual),
                expected, actual
        );
    }

    /**
     * Checks that the word (two bytes) at the specified memory address matches the expected value.
     *
     * @param address the memory address to check
     * @param expected the expected word value
     * @throws AssertionError if the actual value doesn't match expected
     */
    public void checkMemoryTwoBytes(int address, int expected) {
        expected &= 0xFFFF;
        Byte[] word = NumberUtils.numbersToBytes(memoryStub.read(address, 2));
        int actual = NumberUtils.readInt(word, memoryStub.getWordReadingStrategy());

        assertEquals(
                String.format("Expected word mem[%04x]=%04x, but was %04x", address, expected, actual),
                expected, actual
        );
    }

    /**
     * Checks that the CPU flags match the specified mask.
     * Implementation depends on the specific CPU architecture.
     *
     * @param mask the flags mask to check
     */
    public abstract void checkFlags(int mask);

    /**
     * Checks that the CPU flags do not match the specified mask (flags are clear).
     * Implementation depends on the specific CPU architecture.
     *
     * @param mask the flags mask to check as not set
     */
    public abstract void checkNotFlags(int mask);
}
