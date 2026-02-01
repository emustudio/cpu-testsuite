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

public class MemoryAddressTest {
    private SimpleCpuRunner cpuRunner;
    private ByteMemoryStub memory;

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU mockCpu = mock(CPU.class);
        cpuRunner = new SimpleCpuRunner(mockCpu, memory);
    }

    @Test
    public void testByteConstructor() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0x42);
        assertNotNull(injector);
    }

    @Test
    public void testIntegerConstructor() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0x1234);
        assertNotNull(injector);
    }

    @Test
    public void testAcceptByteValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0x55);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0x55, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWordValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0x1234);
        injector.accept(cpuRunner, 0x100);

        // Word value: little-endian
        assertEquals((byte) 0x34, memory.read(0x100).byteValue());
        assertEquals((byte) 0x12, memory.read(0x101).byteValue());
    }

    @Test
    public void testByteValueOnlyWritesOneByte() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0x99);

        // Set a marker at address + 1
        memory.write(0x101, (byte) 0xAA);

        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0x99, memory.read(0x100).byteValue());
        assertEquals((byte) 0xAA, memory.read(0x101).byteValue()); // Should be unchanged
    }

    @Test
    public void testWordValueWritesTwoBytes() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0xABCD);
        injector.accept(cpuRunner, 0x200);

        assertEquals((byte) 0xCD, memory.read(0x200).byteValue());
        assertEquals((byte) 0xAB, memory.read(0x201).byteValue());
    }

    @Test
    public void testByteValueTruncation() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0xFF);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
    }

    @Test
    public void testWordValueTruncation() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0x12345);
        injector.accept(cpuRunner, 0x100);

        // Should truncate to 16 bits: 0x2345
        assertEquals((byte) 0x45, memory.read(0x100).byteValue());
        assertEquals((byte) 0x23, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptAtDifferentAddresses() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0x77);

        injector.accept(cpuRunner, 0x100);
        injector.accept(cpuRunner, 0x200);
        injector.accept(cpuRunner, 0x300);

        assertEquals((byte) 0x77, memory.read(0x100).byteValue());
        assertEquals((byte) 0x77, memory.read(0x200).byteValue());
        assertEquals((byte) 0x77, memory.read(0x300).byteValue());
    }

    @Test
    public void testAcceptWithZeroByteValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithZeroWordValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(Integer.valueOf(0));
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0, memory.read(0x100).byteValue());
        assertEquals((byte) 0, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptWithMaxByteValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0xFF);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithMaxWordValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0xFFFF);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
        assertEquals((byte) 0xFF, memory.read(0x101).byteValue());
    }

    @Test
    public void testToStringForByte() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0x42);
        String result = injector.toString();

        assertNotNull(result);
        assertTrue(result.contains("memory[address]"));
        assertTrue(result.contains("word=false"));
    }

    @Test
    public void testToStringForWord() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0x1234);
        String result = injector.toString();

        assertNotNull(result);
        assertTrue(result.contains("memory[address]"));
        assertTrue(result.contains("word=true"));
    }

    @Test
    public void testAcceptWithByteOperandType() {
        MemoryAddress<SimpleCpuRunner, Byte> injector = new MemoryAddress<>((byte) 0x88);
        injector.accept(cpuRunner, (byte) 0x50);

        assertEquals((byte) 0x88, memory.read(0x50).byteValue());
    }

    @Test
    public void testAcceptWithShortOperandType() {
        MemoryAddress<SimpleCpuRunner, Short> injector = new MemoryAddress<>(0xABCD);
        injector.accept(cpuRunner, (short) 0x100);

        assertEquals((byte) 0xCD, memory.read(0x100).byteValue());
        assertEquals((byte) 0xAB, memory.read(0x101).byteValue());
    }

    @Test
    public void testAcceptOverwritesMemory() {
        MemoryAddress<SimpleCpuRunner, Integer> injector1 = new MemoryAddress<>((byte) 0x11);
        MemoryAddress<SimpleCpuRunner, Integer> injector2 = new MemoryAddress<>((byte) 0x22);

        injector1.accept(cpuRunner, 0x100);
        injector2.accept(cpuRunner, 0x100);

        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
    }

    @Test
    public void testNegativeByteValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) -1);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
    }

    @Test
    public void testNegativeWordValue() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(Integer.valueOf(-1));
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
        assertEquals((byte) 0xFF, memory.read(0x101).byteValue());
    }

    @Test
    public void testWordValueWithHighBitSet() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>(0x8000);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0x00, memory.read(0x100).byteValue());
        assertEquals((byte) 0x80, memory.read(0x101).byteValue());
    }

    @Test
    public void testByteValueWithHighBitSet() {
        MemoryAddress<SimpleCpuRunner, Integer> injector = new MemoryAddress<>((byte) 0x80);
        injector.accept(cpuRunner, 0x100);

        assertEquals((byte) 0x80, memory.read(0x100).byteValue());
    }
}
