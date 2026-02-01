/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShortMemoryStubTest {
    private ShortMemoryStub memory;

    @Before
    public void setUp() {
        memory = new ShortMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void testInitialMemoryIsZero() {
        for (int i = 0; i < 100; i++) {
            assertEquals(0, memory.read(i).shortValue());
        }
    }

    @Test
    public void testReadWriteSingleShort() {
        memory.write(10, (short) 0x1234);
        assertEquals((short) 0x1234, memory.read(10).shortValue());
    }

    @Test
    public void testReadWriteMultipleShorts() {
        Short[] data = {(short) 0x1000, (short) 0x2000, (short) 0x3000, (short) 0x4000};
        memory.write(0, data, 4);

        Short[] result = memory.read(0, 4);
        assertArrayEquals(data, result);
    }

    @Test
    public void testReadMultipleShortsWithBoundary() {
        // Set up memory with known values
        for (int i = 990; i < 1000; i++) {
            memory.write(i, (short) (i * 10));
        }

        // Read from position 995 with count 10, should only return 5 shorts (995-999)
        Short[] result = memory.read(995, 10);
        assertEquals(5, result.length);
        assertEquals((short) (995 * 10), result[0].shortValue());
        assertEquals((short) (999 * 10), result[4].shortValue());
    }

    @Test
    public void testSetMemoryFromShortArray() {
        short[] data = {0x1000, 0x2000, 0x3000, 0x4000, 0x5000};
        memory.setMemory(data);

        assertEquals(0x1000, memory.read(0).shortValue());
        assertEquals(0x2000, memory.read(1).shortValue());
        assertEquals(0x3000, memory.read(2).shortValue());
        assertEquals(0x4000, memory.read(3).shortValue());
        assertEquals(0x5000, memory.read(4).shortValue());
    }

    @Test
    public void testClear() {
        // Write some data
        for (int i = 0; i < 50; i++) {
            memory.write(i, (short) (i + 100));
        }

        // Clear memory
        memory.clear();

        // Verify all shorts are zero
        for (int i = 0; i < 50; i++) {
            assertEquals(0, memory.read(i).shortValue());
        }
    }

    @Test
    public void testGetSize() {
        assertEquals(1000, memory.getSize());
    }

    @Test
    public void testGetCellTypeClass() {
        assertEquals(Short.class, memory.getCellTypeClass());
    }

    @Test
    public void testMemoryNotificationsDisabled() {
        assertFalse(memory.areMemoryNotificationsEnabled());
    }

    @Test
    public void testSetMemoryNotificationsEnabledDoesNothing() {
        // Should not throw an exception
        memory.setMemoryNotificationsEnabled(true);
        assertFalse(memory.areMemoryNotificationsEnabled());
    }

    @Test
    public void testAddMemoryListenerDoesNothing() {
        // Should not throw an exception
        memory.addMemoryListener(null);
    }

    @Test
    public void testRemoveMemoryListenerDoesNothing() {
        // Should not throw an exception
        memory.removeMemoryListener(null);
    }

    @Test
    public void testAnnotationsReturnsNull() {
        assertNull(memory.annotations());
    }

    @Test
    public void testGetWordReadingStrategy() {
        ShortMemoryStub littleEndian = new ShortMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        assertEquals(NumberUtils.Strategy.LITTLE_ENDIAN, littleEndian.getWordReadingStrategy());

        ShortMemoryStub bigEndian = new ShortMemoryStub(NumberUtils.Strategy.BIG_ENDIAN);
        assertEquals(NumberUtils.Strategy.BIG_ENDIAN, bigEndian.getWordReadingStrategy());
    }

    @Test
    public void testWriteAndReadAtDifferentPositions() {
        memory.write(0, (short) 0x1111);
        memory.write(100, (short) 0x2222);
        memory.write(500, (short) 0x3333);
        memory.write(999, (short) 0x4444);

        assertEquals((short) 0x1111, memory.read(0).shortValue());
        assertEquals((short) 0x2222, memory.read(100).shortValue());
        assertEquals((short) 0x3333, memory.read(500).shortValue());
        assertEquals((short) 0x4444, memory.read(999).shortValue());
    }

    @Test
    public void testWriteArrayAtOffset() {
        Short[] data = {(short) 0xAAAA, (short) 0xBBBB, (short) 0xCCCC};
        memory.write(50, data, 3);

        assertEquals((short) 0xAAAA, memory.read(50).shortValue());
        assertEquals((short) 0xBBBB, memory.read(51).shortValue());
        assertEquals((short) 0xCCCC, memory.read(52).shortValue());
    }

    @Test
    public void testOverwriteExistingData() {
        memory.write(10, (short) 0x1111);
        assertEquals((short) 0x1111, memory.read(10).shortValue());

        memory.write(10, (short) 0x2222);
        assertEquals((short) 0x2222, memory.read(10).shortValue());
    }

    @Test
    public void testReadRangeReturnsCorrectLength() {
        Short[] result = memory.read(0, 50);
        assertEquals(50, result.length);

        result = memory.read(0, 10);
        assertEquals(10, result.length);
    }

    @Test
    public void testNegativeShortValues() {
        memory.write(0, (short) -1);
        memory.write(1, (short) -32768);
        memory.write(2, (short) -1000);

        assertEquals((short) -1, memory.read(0).shortValue());
        assertEquals((short) -32768, memory.read(1).shortValue());
        assertEquals((short) -1000, memory.read(2).shortValue());
    }

    @Test
    public void testMaxShortValue() {
        memory.write(0, Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, memory.read(0).shortValue());
    }

    @Test
    public void testMinShortValue() {
        memory.write(0, Short.MIN_VALUE);
        assertEquals(Short.MIN_VALUE, memory.read(0).shortValue());
    }

    @Test
    public void testUnsignedShortValues() {
        memory.write(0, (short) 0xFFFF);
        memory.write(1, (short) 0x8000);

        assertEquals((short) 0xFFFF, memory.read(0).shortValue());
        assertEquals((short) 0x8000, memory.read(1).shortValue());
    }

    @Test
    public void testWritePartialArray() {
        Short[] data = {(short) 0x1111, (short) 0x2222, (short) 0x3333, (short) 0x4444, (short) 0x5555};

        // Write only first 3 elements
        memory.write(10, data, 3);

        assertEquals((short) 0x1111, memory.read(10).shortValue());
        assertEquals((short) 0x2222, memory.read(11).shortValue());
        assertEquals((short) 0x3333, memory.read(12).shortValue());
        assertEquals((short) 0, memory.read(13).shortValue()); // Should remain 0
    }
}
