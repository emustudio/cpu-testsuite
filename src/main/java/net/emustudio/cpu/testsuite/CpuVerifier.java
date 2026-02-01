/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.MemoryStub;
import net.emustudio.emulib.runtime.helpers.NumberUtils;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * CPU Verifier.
 * <p>
 * Used for checking the result (registers, flags, memory) after test execution.
 */
public abstract class CpuVerifier {
    protected final MemoryStub<? extends Number> memoryStub;

    public CpuVerifier(MemoryStub<?> memoryStub) {
        this.memoryStub = Objects.requireNonNull(memoryStub);
    }

    public void checkMemoryByte(int address, int expected) {
        expected &= 0xFF;
        int actual = memoryStub.read(address).intValue() & 0xFF;
        assertEquals(
                String.format("Expected mem[%04x]=%02x, but was %02x", address, expected, actual),
                expected, actual
        );
    }

    public void checkMemoryTwoBytes(int address, int expected) {
        expected &= 0xFFFF;
        Byte[] word = NumberUtils.numbersToBytes(memoryStub.read(address, 2));
        int actual = NumberUtils.readInt(word, memoryStub.getWordReadingStrategy());

        assertEquals(
                String.format("Expected word mem[%04x]=%04x, but was %04x", address, expected, actual),
                expected, actual
        );
    }

    public abstract void checkFlags(int mask);

    public abstract void checkNotFlags(int mask);
}
