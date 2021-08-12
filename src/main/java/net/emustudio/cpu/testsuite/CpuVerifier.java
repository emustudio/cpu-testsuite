/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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

    public void checkMemoryInteger(int address, int value) {
        Byte[] word = NumberUtils.numbersToBytes(memoryStub.read(address, 4));
        int memoryInt = NumberUtils.readInt(word, memoryStub.getWordReadingStrategy());

        assertEquals(
                String.format("Expected word mem[%04x]=%04x, but was %04x", address, value, memoryInt),
                value, memoryInt
        );
    }

    public abstract void checkFlags(int mask);

    public abstract void checkNotFlags(int mask);
}
