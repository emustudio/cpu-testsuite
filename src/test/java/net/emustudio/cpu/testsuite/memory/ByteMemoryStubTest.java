/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class ByteMemoryStubTest extends MemoryStubContractTest<Byte> {
    @Override
    protected ByteMemoryStub createMemory(int strategy) {
        return new ByteMemoryStub(strategy);
    }

    @Override
    protected Byte[] cells(int... values) {
        return IntStream.of(values).mapToObj(value -> (byte) value).toArray(Byte[]::new);
    }

    @Test
    public void primitiveBytesPreserveSignedValues() {
        ByteMemoryStub bytes = (ByteMemoryStub) memory;
        bytes.setMemory(new byte[] {0x12, -1, Byte.MIN_VALUE, Byte.MAX_VALUE});
        assertArrayEquals(cells(0x12, -1, -128, 127), bytes.read(0, 4));
    }

    @Test
    public void boxedBytesBecomeTheBackingCells() {
        Byte[] cells = cells(0x12, 0x34);
        ((ByteMemoryStub) memory).setMemory(cells);
        memory.write(0, (byte) 0x56);
        assertEquals(Byte.valueOf((byte) 0x56), cells[0]);
        assertEquals(2, memory.getSize());
    }

    @Test
    public void shortCellsAreTruncatedToTheirLowByte() {
        memory.setMemory(new short[] {0x1234, (short) 0xABCD, -1});
        assertArrayEquals(cells(0x34, 0xCD, 0xFF), memory.read(0, 3));
        assertEquals(Byte.class, memory.getCellTypeClass());
    }
}
