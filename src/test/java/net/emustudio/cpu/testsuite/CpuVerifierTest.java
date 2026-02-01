/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.memory.ShortMemoryStub;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CpuVerifierTest {
    private TestCpuVerifier verifier;
    private ByteMemoryStub memory;

    // Concrete implementation for testing
    private static class TestCpuVerifier extends CpuVerifier {
        private int flags = 0;

        public TestCpuVerifier(ByteMemoryStub memoryStub) {
            super(memoryStub);
        }

        public void setFlags(int flags) {
            this.flags = flags;
        }

        @Override
        public void checkFlags(int mask) {
            assertEquals("Expected flags " + mask + " to be set", mask, flags & mask);
        }

        @Override
        public void checkNotFlags(int mask) {
            assertEquals("Expected flags " + mask + " to not be set", 0, flags & mask);
        }
    }

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        verifier = new TestCpuVerifier(memory);
    }

    @Test
    public void testCheckMemoryByteSuccess() {
        memory.write(0x100, (byte) 0x42);
        verifier.checkMemoryByte(0x100, 0x42);
    }

    @Test
    public void testCheckMemoryByteWithMask() {
        memory.write(0x100, (byte) 0xFF);
        verifier.checkMemoryByte(0x100, 0x1FF); // Should mask to 0xFF
    }

    @Test(expected = AssertionError.class)
    public void testCheckMemoryByteFailure() {
        memory.write(0x100, (byte) 0x42);
        verifier.checkMemoryByte(0x100, 0x43);
    }

    @Test
    public void testCheckMemoryByteZero() {
        memory.write(0x100, (byte) 0x00);
        verifier.checkMemoryByte(0x100, 0x00);
    }

    @Test
    public void testCheckMemoryByteNegativeValue() {
        memory.write(0x100, (byte) -1); // 0xFF
        verifier.checkMemoryByte(0x100, 0xFF);
    }

    @Test
    public void testCheckMemoryTwoBytesLittleEndian() {
        memory.write(0x100, (byte) 0x34);
        memory.write(0x101, (byte) 0x12);
        verifier.checkMemoryTwoBytes(0x100, 0x1234);
    }

    @Test
    public void testCheckMemoryTwoBytesBigEndian() {
        ByteMemoryStub bigEndianMemory = new ByteMemoryStub(NumberUtils.Strategy.BIG_ENDIAN);
        TestCpuVerifier bigEndianVerifier = new TestCpuVerifier(bigEndianMemory);

        bigEndianMemory.write(0x100, (byte) 0x12);
        bigEndianMemory.write(0x101, (byte) 0x34);
        bigEndianVerifier.checkMemoryTwoBytes(0x100, 0x1234);
    }

    @Test
    public void testCheckMemoryTwoBytesWithMask() {
        memory.write(0x100, (byte) 0x34);
        memory.write(0x101, (byte) 0x12);
        verifier.checkMemoryTwoBytes(0x100, 0x11234); // Should mask to 0x1234
    }

    @Test(expected = AssertionError.class)
    public void testCheckMemoryTwoBytesFailure() {
        memory.write(0x100, (byte) 0x34);
        memory.write(0x101, (byte) 0x12);
        verifier.checkMemoryTwoBytes(0x100, 0x1235);
    }

    @Test
    public void testCheckMemoryTwoBytesZero() {
        memory.write(0x100, (byte) 0x00);
        memory.write(0x101, (byte) 0x00);
        verifier.checkMemoryTwoBytes(0x100, 0x0000);
    }

    @Test
    public void testCheckMemoryTwoBytesMaxValue() {
        memory.write(0x100, (byte) 0xFF);
        memory.write(0x101, (byte) 0xFF);
        verifier.checkMemoryTwoBytes(0x100, 0xFFFF);
    }

    @Test
    public void testCheckFlagsSet() {
        verifier.setFlags(0b10101010);
        verifier.checkFlags(0b10000000);
        verifier.checkFlags(0b00100000);
        verifier.checkFlags(0b00001000);
        verifier.checkFlags(0b00000010);
    }

    @Test
    public void testCheckFlagsMultipleBits() {
        verifier.setFlags(0b11110000);
        verifier.checkFlags(0b11110000);
        verifier.checkFlags(0b11000000);
        verifier.checkFlags(0b00110000);
    }

    @Test(expected = AssertionError.class)
    public void testCheckFlagsNotSet() {
        verifier.setFlags(0b10101010);
        verifier.checkFlags(0b01000000); // This flag is not set
    }

    @Test
    public void testCheckNotFlagsNotSet() {
        verifier.setFlags(0b10101010);
        verifier.checkNotFlags(0b01000000);
        verifier.checkNotFlags(0b00010000);
        verifier.checkNotFlags(0b00000100);
        verifier.checkNotFlags(0b00000001);
    }

    @Test
    public void testCheckNotFlagsMultipleBits() {
        verifier.setFlags(0b00001111);
        verifier.checkNotFlags(0b11110000);
        verifier.checkNotFlags(0b11000000);
        verifier.checkNotFlags(0b00110000);
    }

    @Test(expected = AssertionError.class)
    public void testCheckNotFlagsButAreSet() {
        verifier.setFlags(0b10101010);
        verifier.checkNotFlags(0b10000000); // This flag is set
    }

    @Test
    public void testCheckFlagsZero() {
        verifier.setFlags(0);
        verifier.checkNotFlags(0xFF);
    }

    @Test
    public void testCheckMemoryMultipleLocations() {
        memory.write(0x00, (byte) 0x11);
        memory.write(0x01, (byte) 0x22);
        memory.write(0x02, (byte) 0x33);
        memory.write(0x03, (byte) 0x44);

        verifier.checkMemoryByte(0x00, 0x11);
        verifier.checkMemoryByte(0x01, 0x22);
        verifier.checkMemoryByte(0x02, 0x33);
        verifier.checkMemoryByte(0x03, 0x44);
    }

    @Test
    public void testCheckMemoryTwoBytesMultipleLocations() {
        memory.write(0x00, (byte) 0x34);
        memory.write(0x01, (byte) 0x12);
        memory.write(0x02, (byte) 0x78);
        memory.write(0x03, (byte) 0x56);

        verifier.checkMemoryTwoBytes(0x00, 0x1234);
        verifier.checkMemoryTwoBytes(0x02, 0x5678);
    }

    @Test
    public void testCheckMemoryByteMultipleDifferentAddresses() {
        // Test checking memory at various addresses
        memory.write(0x50, (byte) 0xAA);
        memory.write(0x100, (byte) 0xBB);
        memory.write(0x200, (byte) 0xCC);

        verifier.checkMemoryByte(0x50, 0xAA);
        verifier.checkMemoryByte(0x100, 0xBB);
        verifier.checkMemoryByte(0x200, 0xCC);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullMemory() {
        new TestCpuVerifier(null);
    }

    @Test
    public void testCheckMemoryAtBoundary() {
        // Test at memory boundaries
        memory.write(0, (byte) 0xAA);
        memory.write(999, (byte) 0xBB);

        verifier.checkMemoryByte(0, 0xAA);
        verifier.checkMemoryByte(999, 0xBB);
    }
}
