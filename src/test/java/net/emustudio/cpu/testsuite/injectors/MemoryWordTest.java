/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MemoryWordTest {
    private SimpleCpuRunner cpuRunner;
    private ByteMemoryStub memory;

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU mockCpu = mock(CPU.class);
        cpuRunner = new SimpleCpuRunner(mockCpu, memory);
    }

    @Test
    public void testConstructorWithValidAddress() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        assertNotNull(injector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeAddress() {
        new MemoryWord<SimpleCpuRunner, Integer>(-1);
    }

    @Test
    public void testConstructorWithZeroAddress() {
        // Address 0 is now valid
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0);
        assertNotNull(injector);
    }

    @Test
    public void testAcceptWithIntegerValue() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0x1234);

        // Little-endian: low byte first
        assertEquals((byte) 0x34, memory.read(0x100).byteValue());
        assertEquals((byte) 0x12, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptWithZeroValue() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0);

        assertEquals((byte) 0x00, memory.read(0x100).byteValue());
        assertEquals((byte) 0x00, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptWithMaxValue() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0xFFFF);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
        assertEquals((byte) 0xFF, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptTruncatesHigherBits() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0x12345678);

        // Only low 16 bits should be stored
        assertEquals((byte) 0x78, memory.read(0x100).byteValue());
        assertEquals((byte) 0x56, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptAtDifferentAddresses() {
        MemoryWord<SimpleCpuRunner, Integer> injector1 = new MemoryWord<>(0x100);
        MemoryWord<SimpleCpuRunner, Integer> injector2 = new MemoryWord<>(0x200);

        injector1.accept(cpuRunner, 0xAABB);
        injector2.accept(cpuRunner, 0xCCDD);

        assertEquals((byte) 0xBB, memory.read(0x100).byteValue());
        assertEquals((byte) 0xAA, memory.read(0x101).byteValue());
        assertEquals((byte) 0xDD, memory.read(0x200).byteValue());
        assertEquals((byte) 0xCC, memory.read(0x201).byteValue());
    }

    @Test
    public void testAcceptWithShortValue() {
        MemoryWord<SimpleCpuRunner, Short> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, (short) 0x8765);

        assertEquals((byte) 0x65, memory.read(0x100).byteValue());
        assertEquals((byte) 0x87, memory.read(0x101).byteValue());
    }

    @Test
    public void testToString() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x1234);
        String result = injector.toString();

        assertNotNull(result);
        assertTrue(result.contains("memoryWord"));
        assertTrue(result.contains("1234"));
    }

    @Test
    public void testAcceptOverwritesExistingValue() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);

        injector.accept(cpuRunner, 0x1111);
        injector.accept(cpuRunner, 0x2222);

        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
        assertEquals((byte) 0x22, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptWritesTwoConsecutiveBytes() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0xABCD);

        // Verify both bytes are written
        assertNotEquals((byte) 0x00, memory.read(0x100).byteValue());
        assertNotEquals((byte) 0x00, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptWithSmallValue() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0x0012);

        assertEquals((byte) 0x12, memory.read(0x100).byteValue());
        assertEquals((byte) 0x00, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptPreservesOtherMemory() {
        memory.write(0xFF, (byte) 0x99);
        memory.write(0x102, (byte) 0x88);

        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, 0x1234);

        // Surrounding memory should be preserved
        assertEquals((byte) 0x99, memory.read(0xFF).byteValue());
        assertEquals((byte) 0x88, memory.read(0x102).byteValue());
    }

    @Test
    public void testAcceptWithNegativeValue() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0x100);
        injector.accept(cpuRunner, -1);

        // -1 as 16-bit = 0xFFFF
        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
        assertEquals((byte) 0xFF, memory.read(0x101).byteValue());
    }

    @Test
    public void testMultipleWordsAtAdjacentAddresses() {
        MemoryWord<SimpleCpuRunner, Integer> injector1 = new MemoryWord<>(0x100);
        MemoryWord<SimpleCpuRunner, Integer> injector2 = new MemoryWord<>(0x102);

        injector1.accept(cpuRunner, 0x1122);
        injector2.accept(cpuRunner, 0x3344);

        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
        assertEquals((byte) 0x11, memory.read(0x101).byteValue());
        assertEquals((byte) 0x44, memory.read(0x102).byteValue());
        assertEquals((byte) 0x33, memory.read(0x103).byteValue());
    }

    @Test
    public void testAcceptWithLargeAddress() {
        MemoryWord<SimpleCpuRunner, Integer> injector = new MemoryWord<>(0xFFFE);
        injector.accept(cpuRunner, 0x9988);

        assertEquals((byte) 0x88, memory.read(0xFFFE).byteValue());
        assertEquals((byte) 0x99, memory.read(0xFFFF).byteValue());
    }
}
