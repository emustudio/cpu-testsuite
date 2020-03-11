// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.MemoryStub;
import net.emustudio.emulib.runtime.helpers.NumberUtils;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 *CPU Verifier.
 *
 * Used for checking the result (registers, flags, memory) after test execution.
 */
@SuppressWarnings("unchecked")
public abstract class CpuVerifier {
    protected final MemoryStub<? extends Number> memoryStub;

    public CpuVerifier(MemoryStub<?> memoryStub) {
        this.memoryStub = Objects.requireNonNull(memoryStub);
    }

    public void checkMemoryByte(int address, int value) {
        value &= 0xFF;
        assertEquals(
                String.format("Expected mem[%04x]=%02x, but was %02x", address, value, memoryStub.read(address)),
                value, memoryStub.read(address).intValue()
        );
    }

    public void checkMemoryWord(int address, int value) {
        Number[] read = memoryStub.readWord(address);
        Byte[] word = new Byte[] {0,0,0,0};
        for (int i = 0; i < read.length; i++) {
            word[i] = read[i].byteValue();
        }

        int memoryWord = NumberUtils.readInt(word, memoryStub.getWordReadingStrategy());

        assertEquals(
                String.format("Expected word mem[%04x]=%04x, but was %04x", address, value, memoryWord),
                value, memoryWord
        );
    }

    public abstract void checkFlags(int mask);

    public abstract void checkNotFlags(int mask);
}
