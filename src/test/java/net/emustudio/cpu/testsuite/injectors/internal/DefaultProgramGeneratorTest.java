/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors.internal;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultProgramGeneratorTest {
    private DefaultProgramGenerator<Byte> byteGenerator;
    private DefaultProgramGenerator<Integer> intGenerator;

    @Before
    public void setUp() {
        byteGenerator = new DefaultProgramGenerator<>();
        intGenerator = new DefaultProgramGenerator<>();
    }

    @Test
    public void testGenerateEmptyProgram() {
        List<Short> program = byteGenerator.generate();
        assertNotNull(program);
        assertEquals(0, program.size());
    }

    @Test
    public void testAddOpcodesOnly() {
        byteGenerator.addOpcodes(0x10, 0x20, 0x30);
        List<Short> program = byteGenerator.generate();

        assertEquals(3, program.size());
        assertEquals(0x10, program.get(0).intValue());
        assertEquals(0x20, program.get(1).intValue());
        assertEquals(0x30, program.get(2).intValue());
    }

    @Test
    public void testSetOperandsByte() {
        byteGenerator.setOperands((byte) 0x42, (byte) 0x55);
        List<Short> program = byteGenerator.generate();

        assertEquals(2, program.size());
        assertEquals(0x42, program.get(0).intValue());
        assertEquals(0x55, program.get(1).intValue());
    }

    @Test
    public void testSetOperandsInteger() {
        intGenerator.setOperands(0x1234, 0x5678);
        List<Short> program = intGenerator.generate();

        // Each integer should produce 2 bytes (little-endian)
        assertEquals(4, program.size());
        assertEquals(0x34, program.get(0).intValue());
        assertEquals(0x12, program.get(1).intValue());
        assertEquals(0x78, program.get(2).intValue());
        assertEquals(0x56, program.get(3).intValue());
    }

    @Test
    public void testAddOpcodesAfterOperands() {
        byteGenerator.addOpcodesAfterOperands(0xAA, 0xBB);
        List<Short> program = byteGenerator.generate();

        assertEquals(2, program.size());
        assertEquals(0xAA, program.get(0).intValue());
        assertEquals(0xBB, program.get(1).intValue());
    }

    @Test
    public void testCompleteProgram() {
        byteGenerator.addOpcodes(0x01, 0x02);
        byteGenerator.setOperands((byte) 0x10, (byte) 0x20);
        byteGenerator.addOpcodesAfterOperands(0x03, 0x04);

        List<Short> program = byteGenerator.generate();

        assertEquals(6, program.size());
        // Opcodes first
        assertEquals(0x01, program.get(0).intValue());
        assertEquals(0x02, program.get(1).intValue());
        // Then operands
        assertEquals(0x10, program.get(2).intValue());
        assertEquals(0x20, program.get(3).intValue());
        // Then opcodes after operands
        assertEquals(0x03, program.get(4).intValue());
        assertEquals(0x04, program.get(5).intValue());
    }

    @Test
    public void testCompleteProgramWithIntegerOperands() {
        intGenerator.addOpcodes(0xFF);
        intGenerator.setOperands(0x1234);
        intGenerator.addOpcodesAfterOperands(0xEE);

        List<Short> program = intGenerator.generate();

        assertEquals(4, program.size());
        assertEquals(0xFF, program.get(0).intValue());
        assertEquals(0x34, program.get(1).intValue()); // Low byte
        assertEquals(0x12, program.get(2).intValue()); // High byte
        assertEquals(0xEE, program.get(3).intValue());
    }

    @Test
    public void testClearOperands() {
        byteGenerator.setOperands((byte) 0x10, (byte) 0x20, (byte) 0x30);
        byteGenerator.clearOperands();

        List<Short> program = byteGenerator.generate();
        assertEquals(0, program.size());
    }

    @Test
    public void testClearOperandsDoesNotAffectOpcodes() {
        byteGenerator.addOpcodes(0x01, 0x02);
        byteGenerator.setOperands((byte) 0x10, (byte) 0x20);
        byteGenerator.clearOperands();

        List<Short> program = byteGenerator.generate();
        assertEquals(2, program.size());
        assertEquals(0x01, program.get(0).intValue());
        assertEquals(0x02, program.get(1).intValue());
    }

    @Test
    public void testClearOperandsDoesNotAffectOpcodesAfter() {
        byteGenerator.addOpcodesAfterOperands(0x01, 0x02);
        byteGenerator.setOperands((byte) 0x10, (byte) 0x20);
        byteGenerator.clearOperands();

        List<Short> program = byteGenerator.generate();
        assertEquals(2, program.size());
        assertEquals(0x01, program.get(0).intValue());
        assertEquals(0x02, program.get(1).intValue());
    }

    @Test
    public void testMultipleAddOpcodes() {
        byteGenerator.addOpcodes(0x01);
        byteGenerator.addOpcodes(0x02);
        byteGenerator.addOpcodes(0x03);

        List<Short> program = byteGenerator.generate();
        assertEquals(3, program.size());
        assertEquals(0x01, program.get(0).intValue());
        assertEquals(0x02, program.get(1).intValue());
        assertEquals(0x03, program.get(2).intValue());
    }

    @Test
    public void testMultipleSetOperands() {
        byteGenerator.setOperands((byte) 0x01);
        byteGenerator.setOperands((byte) 0x02);

        List<Short> program = byteGenerator.generate();
        assertEquals(2, program.size());
        assertEquals(0x01, program.get(0).intValue());
        assertEquals(0x02, program.get(1).intValue());
    }

    @Test
    public void testMultipleAddOpcodesAfterOperands() {
        byteGenerator.addOpcodesAfterOperands(0x01);
        byteGenerator.addOpcodesAfterOperands(0x02);

        List<Short> program = byteGenerator.generate();
        assertEquals(2, program.size());
        assertEquals(0x01, program.get(0).intValue());
        assertEquals(0x02, program.get(1).intValue());
    }

    @Test
    public void testByteOperandWithNegativeValue() {
        byteGenerator.setOperands((byte) -1);
        List<Short> program = byteGenerator.generate();

        assertEquals(1, program.size());
        assertEquals(0xFF, program.get(0).intValue());
    }

    @Test
    public void testIntegerOperandWithNegativeValue() {
        intGenerator.setOperands(-1);
        List<Short> program = intGenerator.generate();

        assertEquals(2, program.size());
        assertEquals(0xFF, program.get(0).intValue());
        assertEquals(0xFF, program.get(1).intValue());
    }

    @Test
    public void testIntegerOperandLittleEndian() {
        intGenerator.setOperands(0xABCD);
        List<Short> program = intGenerator.generate();

        assertEquals(2, program.size());
        assertEquals(0xCD, program.get(0).intValue()); // Low byte first
        assertEquals(0xAB, program.get(1).intValue()); // High byte second
    }

    @Test
    public void testMaxByteValue() {
        byteGenerator.setOperands((byte) 0xFF);
        List<Short> program = byteGenerator.generate();

        assertEquals(1, program.size());
        assertEquals(0xFF, program.get(0).intValue());
    }

    @Test
    public void testMaxIntegerValue16Bit() {
        intGenerator.setOperands(0xFFFF);
        List<Short> program = intGenerator.generate();

        assertEquals(2, program.size());
        assertEquals(0xFF, program.get(0).intValue());
        assertEquals(0xFF, program.get(1).intValue());
    }

    @Test
    public void testZeroOperands() {
        byteGenerator.setOperands((byte) 0);
        intGenerator.setOperands(0);

        List<Short> byteProgram = byteGenerator.generate();
        List<Short> intProgram = intGenerator.generate();

        assertEquals(1, byteProgram.size());
        assertEquals(0, byteProgram.get(0).intValue());

        assertEquals(2, intProgram.size());
        assertEquals(0, intProgram.get(0).intValue());
        assertEquals(0, intProgram.get(1).intValue());
    }

    @Test
    public void testToString() {
        byteGenerator.addOpcodes(0x01, 0x02);
        byteGenerator.setOperands((byte) 0x10);
        byteGenerator.addOpcodesAfterOperands(0x03);

        String result = byteGenerator.toString();
        assertNotNull(result);
        assertTrue(result.contains("instruction:"));
    }

    @Test
    public void testGenerateMultipleTimes() {
        byteGenerator.addOpcodes(0x01, 0x02);
        byteGenerator.setOperands((byte) 0x10);

        List<Short> program1 = byteGenerator.generate();
        List<Short> program2 = byteGenerator.generate();

        assertEquals(program1.size(), program2.size());
        for (int i = 0; i < program1.size(); i++) {
            assertEquals(program1.get(i), program2.get(i));
        }
    }

    @Test
    public void testMixedByteOperands() {
        byteGenerator.setOperands((byte) 0x00, (byte) 0xFF, (byte) 0x7F, (byte) 0x80);
        List<Short> program = byteGenerator.generate();

        assertEquals(4, program.size());
        assertEquals(0x00, program.get(0).intValue());
        assertEquals(0xFF, program.get(1).intValue());
        assertEquals(0x7F, program.get(2).intValue());
        assertEquals(0x80, program.get(3).intValue());
    }

    @Test
    public void testMixedIntegerOperands() {
        intGenerator.setOperands(0x0000, 0xFFFF, 0x1234, 0xABCD);
        List<Short> program = intGenerator.generate();

        assertEquals(8, program.size());
        // 0x0000
        assertEquals(0x00, program.get(0).intValue());
        assertEquals(0x00, program.get(1).intValue());
        // 0xFFFF
        assertEquals(0xFF, program.get(2).intValue());
        assertEquals(0xFF, program.get(3).intValue());
        // 0x1234
        assertEquals(0x34, program.get(4).intValue());
        assertEquals(0x12, program.get(5).intValue());
        // 0xABCD
        assertEquals(0xCD, program.get(6).intValue());
        assertEquals(0xAB, program.get(7).intValue());
    }
}
