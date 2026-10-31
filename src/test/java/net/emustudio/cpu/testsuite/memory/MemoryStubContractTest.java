/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.runtime.helpers.NumberUtils.Strategy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class MemoryStubContractTest<T extends Number> {
    protected MemoryStub<T> memory;

    protected abstract MemoryStub<T> createMemory(int strategy);

    protected abstract T[] cells(int... values);

    @Before
    public void setUpMemory() {
        memory = createMemory(Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void initialMemoryIsZeroFilled() {
        assertEquals(1000, memory.getSize());
        for (T value : memory.read(0, memory.getSize())) {
            assertEquals(0, value.intValue());
        }
    }

    @Test
    public void singleCellWritesSupportOffsetsAndOverwrites() {
        for (int address : new int[] {0, 100, 500, 999}) {
            memory.write(address, cells(0x42)[0]);
            assertEquals(0x42, memory.read(address).intValue());
            memory.write(address, cells(0x43)[0]);
            assertEquals(0x43, memory.read(address).intValue());
        }
    }

    @Test
    public void arrayWritesRespectTheOffsetAndCount() {
        T[] data = cells(0x11, 0x22, 0x33, 0x44);
        memory.write(10, data, 3);
        assertArrayEquals(cells(0, 0x11, 0x22, 0x33, 0), memory.read(9, 5));
    }

    @Test
    public void writesPreserveSignedCells() {
        memory.write(0, cells(-1)[0]);
        assertEquals(-1, memory.read(0).intValue());
        memory.write(1, cells(-1, 1), 2);
        assertArrayEquals(cells(-1, -1, 1), memory.read(0, 3));
    }

    @Test
    public void readsStopAtTheEndAndReturnIndependentArrays() {
        memory.write(998, cells(0x11, 0x22), 2);
        T[] result = memory.read(998, 10);
        assertArrayEquals(cells(0x11, 0x22), result);
        result[0] = cells(0)[0];
        assertEquals(0x11, memory.read(998).intValue());
        assertEquals(0, memory.read(1000, 1).length);
    }

    @Test
    public void clearRestoresEveryCellToZero() {
        memory.write(0, cells(1)[0]);
        memory.write(999, cells(-1)[0]);
        memory.clear();
        for (T value : memory.read(0, memory.getSize())) {
            assertEquals(0, value.intValue());
        }
    }

    @Test
    public void notificationsAndAnnotationsRemainDisabled() {
        memory.addMemoryListener(null);
        memory.setMemoryNotificationsEnabled(true);
        assertFalse(memory.areMemoryNotificationsEnabled());
        memory.removeMemoryListener(null);
        assertNull(memory.annotations());
    }

    @Test
    public void suppliedWordStrategyIsPreserved() {
        assertEquals(Strategy.LITTLE_ENDIAN, memory.getWordReadingStrategy());
        int strategy = Strategy.BIG_ENDIAN | Strategy.REVERSE_BITS;
        assertEquals(strategy, createMemory(strategy).getWordReadingStrategy());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void writingPastTheEndFails() {
        memory.write(1000, cells(1)[0]);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void negativeReadsFail() {
        memory.read(-1);
    }
}
