/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MemoryByteTest {
    private TestCpuRunner cpuRunner;
    private ByteMemoryStub memory;

    private static class TestCpuRunner extends CpuRunner<CPU> {
        public TestCpuRunner(CPU cpu, ByteMemoryStub memoryStub) {
            super(cpu, memoryStub);
        }

        @Override
        public int getPC() {
            return 0;
        }

        @Override
        public int getSP() {
            return 0;
        }

        @Override
        public List<Integer> getRegisters() {
            return Collections.emptyList();
        }

        @Override
        public void setRegister(int register, int value) {
        }

        @Override
        public void setFlags(int mask) {
        }

        @Override
        public int getFlags() {
            return 0;
        }
    }

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU mockCpu = mock(CPU.class);
        cpuRunner = new TestCpuRunner(mockCpu, memory);
    }

    @Test
    public void testConstructorWithValidAddress() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        assertNotNull(injector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroAddress() {
        new MemoryByte<TestCpuRunner, Integer>(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeAddress() {
        new MemoryByte<TestCpuRunner, Integer>(-1);
    }

    @Test
    public void testAcceptWithByteValue() {
        MemoryByte<TestCpuRunner, Byte> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, (byte) 0x42);

        assertEquals((byte) 0x42, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithIntegerValue() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x200);
        injector.accept(cpuRunner, 0x55);

        assertEquals((byte) 0x55, memory.read(0x200).byteValue());
    }

    @Test
    public void testAcceptTruncatesHigherBits() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, 0x1234);

        // Only low byte should be stored
        assertEquals((byte) 0x34, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithZeroValue() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, 0);

        assertEquals((byte) 0, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithMaxByteValue() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, 0xFF);

        assertEquals((byte) 0xFF, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithNegativeByteValue() {
        MemoryByte<TestCpuRunner, Byte> injector = new MemoryByte<>(0x100);
        injector.accept(cpuRunner, (byte) -1);

        assertEquals((byte) -1, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptAtDifferentAddresses() {
        MemoryByte<TestCpuRunner, Integer> injector1 = new MemoryByte<>(0x100);
        MemoryByte<TestCpuRunner, Integer> injector2 = new MemoryByte<>(0x200);
        MemoryByte<TestCpuRunner, Integer> injector3 = new MemoryByte<>(0x300);

        injector1.accept(cpuRunner, 0xAA);
        injector2.accept(cpuRunner, 0xBB);
        injector3.accept(cpuRunner, 0xCC);

        assertEquals((byte) 0xAA, memory.read(0x100).byteValue());
        assertEquals((byte) 0xBB, memory.read(0x200).byteValue());
        assertEquals((byte) 0xCC, memory.read(0x300).byteValue());
    }

    @Test
    public void testToString() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x1234);
        String result = injector.toString();

        assertNotNull(result);
        assertTrue(result.contains("memoryByte"));
        assertTrue(result.contains("1234"));
    }

    @Test
    public void testToStringFormatting() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0xABCD);
        String result = injector.toString();

        assertTrue(result.contains("abcd"));
    }

    @Test
    public void testAcceptOverwritesExistingValue() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0x100);

        injector.accept(cpuRunner, 0x11);
        assertEquals((byte) 0x11, memory.read(0x100).byteValue());

        injector.accept(cpuRunner, 0x22);
        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithLargeAddress() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(0xFFFF);
        injector.accept(cpuRunner, 0x99);

        assertEquals((byte) 0x99, memory.read(0xFFFF).byteValue());
    }

    @Test
    public void testMultipleInjectorsOnSameAddress() {
        MemoryByte<TestCpuRunner, Integer> injector1 = new MemoryByte<>(0x100);
        MemoryByte<TestCpuRunner, Integer> injector2 = new MemoryByte<>(0x100);

        injector1.accept(cpuRunner, 0x11);
        injector2.accept(cpuRunner, 0x22);

        // Last write should win
        assertEquals((byte) 0x22, memory.read(0x100).byteValue());
    }

    @Test
    public void testAcceptWithSmallPositiveAddress() {
        MemoryByte<TestCpuRunner, Integer> injector = new MemoryByte<>(1);
        injector.accept(cpuRunner, 0x77);

        assertEquals((byte) 0x77, memory.read(1).byteValue());
    }
}
