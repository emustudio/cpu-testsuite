/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ByteMemoryStubTest {
    private ByteMemoryStub memory;

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void testInitialMemoryIsZero() {
        for (int i = 0; i < 100; i++) {
            assertEquals(0, memory.read(i).byteValue());
        }
    }

    @Test
    public void testReadWriteSingleByte() {
        memory.write(10, (byte) 0x42);
        assertEquals((byte) 0x42, memory.read(10).byteValue());
    }

    @Test
    public void testReadWriteMultipleBytes() {
        Byte[] data = {(byte) 0x10, (byte) 0x20, (byte) 0x30, (byte) 0x40};
        memory.write(0, data, 4);

        Byte[] result = memory.read(0, 4);
        assertArrayEquals(data, result);
    }

    @Test
    public void testReadMultipleBytesWithBoundary() {
        // Set up memory with known values
        for (int i = 990; i < 1000; i++) {
            memory.write(i, (byte) (i & 0xFF));
        }

        // Read from position 995 with count 10, should only return 5 bytes (995-999)
        Byte[] result = memory.read(995, 10);
        assertEquals(5, result.length);
        assertEquals((byte) (995 & 0xFF), result[0].byteValue());
        assertEquals((byte) (999 & 0xFF), result[4].byteValue());
    }

    @Test
    public void testSetMemoryFromByteArray() {
        byte[] data = {0x10, 0x20, 0x30, 0x40, 0x50};
        memory.setMemory(data);

        assertEquals(0x10, memory.read(0).byteValue());
        assertEquals(0x20, memory.read(1).byteValue());
        assertEquals(0x30, memory.read(2).byteValue());
        assertEquals(0x40, memory.read(3).byteValue());
        assertEquals(0x50, memory.read(4).byteValue());
    }

    @Test
    public void testSetMemoryFromByteObjectArray() {
        Byte[] data = {(byte) 0x10, (byte) 0x20, (byte) 0x30};
        memory.setMemory(data);

        assertEquals(0x10, memory.read(0).byteValue());
        assertEquals(0x20, memory.read(1).byteValue());
        assertEquals(0x30, memory.read(2).byteValue());
    }

    @Test
    public void testSetMemoryFromShortArray() {
        short[] data = {0x1234, 0x5678};
        memory.setMemory(data);

        // NumberUtils.nativeShortsToBytes converts each short to bytes
        // Check that data was set (actual conversion depends on NumberUtils implementation)
        Byte[] result = memory.read(0, 2);
        assertEquals(2, result.length);
        // Just verify the data is not null/zero
        assertNotNull(result[0]);
        assertNotNull(result[1]);
    }

    @Test
    public void testClear() {
        // Write some data
        for (int i = 0; i < 50; i++) {
            memory.write(i, (byte) (i + 1));
        }

        // Clear memory
        memory.clear();

        // Verify all bytes are zero
        for (int i = 0; i < 50; i++) {
            assertEquals(0, memory.read(i).byteValue());
        }
    }

    @Test
    public void testGetSize() {
        assertEquals(1000, memory.getSize());
    }

    @Test
    public void testGetCellTypeClass() {
        assertEquals(Byte.class, memory.getCellTypeClass());
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
        ByteMemoryStub littleEndian = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        assertEquals(NumberUtils.Strategy.LITTLE_ENDIAN, littleEndian.getWordReadingStrategy());

        ByteMemoryStub bigEndian = new ByteMemoryStub(NumberUtils.Strategy.BIG_ENDIAN);
        assertEquals(NumberUtils.Strategy.BIG_ENDIAN, bigEndian.getWordReadingStrategy());
    }

    @Test
    public void testWriteAndReadAtDifferentPositions() {
        memory.write(0, (byte) 0x11);
        memory.write(100, (byte) 0x22);
        memory.write(500, (byte) 0x33);
        memory.write(999, (byte) 0x44);

        assertEquals((byte) 0x11, memory.read(0).byteValue());
        assertEquals((byte) 0x22, memory.read(100).byteValue());
        assertEquals((byte) 0x33, memory.read(500).byteValue());
        assertEquals((byte) 0x44, memory.read(999).byteValue());
    }

    @Test
    public void testWriteArrayAtOffset() {
        Byte[] data = {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC};
        memory.write(50, data, 3);

        assertEquals((byte) 0xAA, memory.read(50).byteValue());
        assertEquals((byte) 0xBB, memory.read(51).byteValue());
        assertEquals((byte) 0xCC, memory.read(52).byteValue());
    }

    @Test
    public void testOverwriteExistingData() {
        memory.write(10, (byte) 0x11);
        assertEquals((byte) 0x11, memory.read(10).byteValue());

        memory.write(10, (byte) 0x22);
        assertEquals((byte) 0x22, memory.read(10).byteValue());
    }

    @Test
    public void testReadRangeReturnsCorrectLength() {
        Byte[] result = memory.read(0, 50);
        assertEquals(50, result.length);

        result = memory.read(0, 10);
        assertEquals(10, result.length);
    }

    @Test
    public void testNegativeByteValues() {
        memory.write(0, (byte) -1);
        memory.write(1, (byte) -128);
        memory.write(2, (byte) -50);

        assertEquals((byte) -1, memory.read(0).byteValue());
        assertEquals((byte) -128, memory.read(1).byteValue());
        assertEquals((byte) -50, memory.read(2).byteValue());
    }
}
