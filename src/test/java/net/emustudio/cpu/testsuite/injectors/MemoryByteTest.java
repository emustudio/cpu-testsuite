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

public class MemoryByteTest {
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
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        assertNotNull(injector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroAddress() {
        new MemoryByte<SimpleCpuRunner, Integer>(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeAddress() {
        new MemoryByte<SimpleCpuRunner, Integer>(-1);
    }

    @Test
    public void testAcceptWithByteValue() {
        MemoryByte<SimpleCpuRunner, Byte> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, (byte) 0x42);

        assertEquals((byte) 0x42, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithIntegerValue() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x200);
        injector.accept(cpuRunner, 0x55);

        assertEquals((byte) 0x55, memory.read(0x200).byteValue());
    }

    @Test
    public void testAcceptTruncatesHigherBits() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, 0x1234);

        // Only low byte should be stored
        assertEquals((byte) 0x34, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithZeroValue() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, 0);

        assertEquals((byte) 0, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithMaxByteValue() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, 0xFF);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithNegativeByteValue() {
        MemoryByte<SimpleCpuRunner, Byte> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, (byte) -1);

        assertEquals((byte) -1, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptAtDifferentAddresses() {
        MemoryByte<SimpleCpuRunner, Integer> injector1 = new MemoryByte<>(0x100);
        MemoryByte<SimpleCpuRunner, Integer> injector2 = new MemoryByte<>(0x200);
        MemoryByte<SimpleCpuRunner, Integer> injector3 = new MemoryByte<>(0x300);

        injector1.accept(cpuRunner, 0xAA);
        injector2.accept(cpuRunner, 0xBB);
        injector3.accept(cpuRunner, 0xCC);

        assertEquals((byte) 0xAA, memory.read(0x100).byteValue());
        assertEquals((byte) 0xBB, memory.read(0x200).byteValue());
        assertEquals((byte) 0xCC, memory.read(0x300).byteValue());
    }

    @Test
    public void testToString() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x1234);
        String result = injector.toString();

        assertNotNull(result);
        assertTrue(result.contains("memoryByte"));
        assertTrue(result.contains("1234"));
    }

    @Test
    public void testToStringFormatting() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0xABCD);
        String result = injector.toString();

        assertTrue(result.contains("abcd"));
    }

    @Test
    public void testAcceptOverwritesExistingValue() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0x100);

        injector.accept(cpuRunner, 0x11);
        assertEquals((byte) 0x11, memory.read(0x100).byteValue());

        injector.accept(cpuRunner, 0x22);
        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithLargeAddress() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(0xFFFF);
        injector.accept(cpuRunner, 0x99);

        assertEquals((byte) 0x99, memory.read(0xFFFF).byteValue());
    }

    @Test
    public void testMultipleInjectorsOnSameAddress() {
        MemoryByte<SimpleCpuRunner, Integer> injector1 = new MemoryByte<>(0x100);
        MemoryByte<SimpleCpuRunner, Integer> injector2 = new MemoryByte<>(0x100);

        injector1.accept(cpuRunner, 0x11);
        injector2.accept(cpuRunner, 0x22);

        // Last write should win
        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithSmallPositiveAddress() {
        MemoryByte<SimpleCpuRunner, Integer> injector = new MemoryByte<>(1);
        injector.accept(cpuRunner, 0x77);

        assertEquals((byte) 0x77, memory.read(1).byteValue());
    }
}
