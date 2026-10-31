/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class ShortMemoryStubTest extends MemoryStubContractTest<Short> {
    @Override
    protected ShortMemoryStub createMemory(int strategy) {
        return new ShortMemoryStub(strategy);
    }

    @Override
    protected Short[] cells(int... values) {
        return IntStream.of(values).mapToObj(value -> (short) value).toArray(Short[]::new);
    }

    @Test
    public void primitiveShortsPreserveTheEntireCell() {
        memory.setMemory(new short[] {0x1234, -1, Short.MIN_VALUE, Short.MAX_VALUE});
        assertArrayEquals(cells(0x1234, -1, -32768, 32767), memory.read(0, 4));
        assertEquals(Short.class, memory.getCellTypeClass());
    }

    @Test
    public void writesPreserveAllSixteenBits() {
        memory.write(0, (short) 0xABCD);
        memory.write(1, cells(0x1234, Short.MIN_VALUE, Short.MAX_VALUE), 3);
        assertArrayEquals(cells(0xABCD, 0x1234, -32768, 32767), memory.read(0, 4));
    }

    @Test
    public void shortMemoryCopiesPrimitiveCells() {
        short[] source = {0x1234, 0x5678};
        memory.setMemory(source);
        source[0] = 0;
        assertEquals(0x1234, memory.read(0).intValue());
        assertEquals(2, memory.getSize());
    }
}
