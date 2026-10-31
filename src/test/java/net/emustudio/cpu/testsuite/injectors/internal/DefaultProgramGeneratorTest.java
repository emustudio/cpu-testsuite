/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors.internal;

import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultProgramGeneratorTest {
    private static final int LITTLE_ENDIAN = NumberUtils.Strategy.LITTLE_ENDIAN;
    private DefaultProgramGenerator<Byte> byteGenerator;
    private DefaultProgramGenerator<Integer> intGenerator;

    @Before
    public void setUp() {
        byteGenerator = new DefaultProgramGenerator<>();
        intGenerator = new DefaultProgramGenerator<>();
    }

    @Test
    public void generatesEmptyProgram() {
        assertEquals(List.of(), byteGenerator.generate(LITTLE_ENDIAN));
    }

    @Test
    public void combinesOpcodesOperandsAndSuffixInOrder() {
        byteGenerator.addOpcodes(0x01, 0x02);
        byteGenerator.addOpcodesAfterOperands(0x03, 0x04);
        assertEquals(List.of((short) 0x01, (short) 0x02, (short) 0x10, (short) 0x20, (short) 0x03, (short) 0x04),
                byteGenerator.generate(LITTLE_ENDIAN, (byte) 0x10, (byte) 0x20));
    }

    @Test
    public void combinesIntegerOperandsWithOpcodes() {
        intGenerator.addOpcodes(0xFF);
        intGenerator.addOpcodesAfterOperands(0xEE);
        assertEquals(List.of((short) 0xFF, (short) 0x34, (short) 0x12, (short) 0xEE),
                intGenerator.generate(LITTLE_ENDIAN, 0x1234));
    }

    @Test
    public void appendsConfiguredOpcodesAcrossCalls() {
        byteGenerator.addOpcodes(0x01);
        byteGenerator.addOpcodes(0x02);
        byteGenerator.addOpcodesAfterOperands(0x03);
        byteGenerator.addOpcodesAfterOperands(0x04);
        assertEquals(List.of((short) 0x01, (short) 0x02, (short) 0x03, (short) 0x04),
                byteGenerator.generate(LITTLE_ENDIAN));
    }

    @Test
    public void byteOperandsRemainUnsignedBytes() {
        assertEquals(List.of((short) 0x00, (short) 0xFF, (short) 0x7F, (short) 0x80),
                byteGenerator.generate(LITTLE_ENDIAN, (byte) 0x00, (byte) -1, (byte) 0x7F, (byte) 0x80));
    }

    @Test
    public void integerOperandsUseLowSixteenBits() {
        assertEquals(List.of((short) 0x00, (short) 0x00, (short) 0xFF, (short) 0xFF,
                        (short) 0x34, (short) 0x12, (short) 0xCD, (short) 0xAB),
                intGenerator.generate(LITTLE_ENDIAN, 0, -1, 0xABCD1234, 0xABCD));
    }

    @Test
    public void repeatedGenerationDoesNotRetainOperands() {
        byteGenerator.addOpcodes(0x01);
        byteGenerator.addOpcodesAfterOperands(0x02);
        assertEquals(List.of((short) 0x01, (short) 0x10, (short) 0x20, (short) 0x02),
                byteGenerator.generate(LITTLE_ENDIAN, (byte) 0x10, (byte) 0x20));
        assertEquals(List.of((short) 0x01, (short) 0x30, (short) 0x02),
                byteGenerator.generate(LITTLE_ENDIAN, (byte) 0x30));
        assertEquals(List.of((short) 0x01, (short) 0x02), byteGenerator.generate(LITTLE_ENDIAN));
    }

    @Test
    public void modifyingGeneratedProgramDoesNotChangeConfiguration() {
        byteGenerator.addOpcodes(0x01);
        byteGenerator.generate(LITTLE_ENDIAN, (byte) 0x10).clear();
        assertEquals(List.of((short) 0x01, (short) 0x10),
                byteGenerator.generate(LITTLE_ENDIAN, (byte) 0x10));
    }

    @Test
    public void failedGenerationDoesNotRetainPartialOperands() {
        DefaultProgramGenerator<Number> generator = new DefaultProgramGenerator<>();
        generator.addOpcodes(0x01);
        assertThrows(IllegalStateException.class, () -> generator.generate(LITTLE_ENDIAN, (byte) 0x10, 1.0));
        assertEquals(List.of((short) 0x01, (short) 0x20), generator.generate(LITTLE_ENDIAN, (byte) 0x20));
    }

    @Test
    public void describesConfiguredOpcodes() {
        byteGenerator.addOpcodes(0x01, 0x02);
        byteGenerator.addOpcodesAfterOperands(0x03);
        assertEquals("instruction: [01, 02][03]", byteGenerator.toString());
    }
}
