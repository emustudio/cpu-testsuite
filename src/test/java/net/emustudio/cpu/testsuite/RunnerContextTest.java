/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RunnerContextTest {

    @Test
    public void testConstructorWithAllParameters() {
        List<Integer> registers = Arrays.asList(0x10, 0x20, 0x30);
        RunnerContext<Integer> context = new RunnerContext<>(100, 200, 0x42, 0x1000, 0x2000, registers);

        assertEquals(Integer.valueOf(100), context.first);
        assertEquals(Integer.valueOf(200), context.second);
        assertEquals(0x42, context.flags);
        assertEquals(0x1000, context.PC);
        assertEquals(0x2000, context.SP);
        assertEquals(3, context.registers.size());
    }

    @Test
    public void testConstructorWithMinimalParameters() {
        RunnerContext<Byte> context = new RunnerContext<>((byte) 10, (byte) 20, 0xFF);

        assertEquals(Byte.valueOf((byte) 10), context.first);
        assertEquals(Byte.valueOf((byte) 20), context.second);
        assertEquals(0xFF, context.flags);
        assertEquals(0, context.PC);
        assertEquals(0, context.SP);
        assertTrue(context.registers.isEmpty());
    }

    @Test
    public void testGetRegister() {
        List<Integer> registers = Arrays.asList(0xAA, 0xBB, 0xCC, 0xDD);
        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0, 0, 0, registers);

        assertEquals(0xAA, context.getRegister(0));
        assertEquals(0xBB, context.getRegister(1));
        assertEquals(0xCC, context.getRegister(2));
        assertEquals(0xDD, context.getRegister(3));
    }

    @Test
    public void testSwitchFirstAndSecond() {
        List<Integer> registers = Arrays.asList(1, 2, 3);
        RunnerContext<Integer> original = new RunnerContext<>(100, 200, 0x42, 0x1000, 0x2000, registers);

        RunnerContext<Integer> switched = original.switchFirstAndSecond();

        assertEquals(Integer.valueOf(200), switched.first);
        assertEquals(Integer.valueOf(100), switched.second);
        assertEquals(0x42, switched.flags);
        assertEquals(0x1000, switched.PC);
        assertEquals(0x2000, switched.SP);
        assertEquals(3, switched.registers.size());
    }

    @Test
    public void testSwitchFirstAndSecondPreservesRegisters() {
        List<Integer> registers = Arrays.asList(0x10, 0x20, 0x30);
        RunnerContext<Byte> original = new RunnerContext<>((byte) 5, (byte) 10, 0, 0, 0, registers);

        RunnerContext<Byte> switched = original.switchFirstAndSecond();

        assertEquals(0x10, switched.getRegister(0));
        assertEquals(0x20, switched.getRegister(1));
        assertEquals(0x30, switched.getRegister(2));
    }

    @Test
    public void testRegistersAreImmutable() {
        List<Integer> registers = new ArrayList<>(Arrays.asList(1, 2, 3));
        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0, 0, 0, registers);

        // Try to modify the original list
        registers.clear();

        // Context should still have the original values
        assertEquals(3, context.registers.size());
        assertEquals(1, context.getRegister(0));
    }

    @Test
    public void testToStringWithByteOperands() {
        RunnerContext<Byte> context = new RunnerContext<>((byte) 0x12, (byte) 0x34, 0xAB, 0x1000, 0x2000,
                Arrays.asList(0x11, 0x22));

        String result = context.toString();

        assertNotNull(result);
        assertTrue(result.contains("RunnerContext"));
        assertTrue(result.contains("operands"));
        assertTrue(result.contains("flags"));
        assertTrue(result.contains("PC"));
        assertTrue(result.contains("SP"));
        assertTrue(result.contains("registers"));
    }

    @Test
    public void testToStringWithIntegerOperands() {
        RunnerContext<Integer> context = new RunnerContext<>(0x1234, 0x5678, 0xFF, 0x100, 0x200,
                Collections.emptyList());

        String result = context.toString();

        assertNotNull(result);
        assertTrue(result.contains("RunnerContext"));
    }

    @Test
    public void testContextWithZeroValues() {
        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0, 0, 0, Collections.emptyList());

        assertEquals(Integer.valueOf(0), context.first);
        assertEquals(Integer.valueOf(0), context.second);
        assertEquals(0, context.flags);
        assertEquals(0, context.PC);
        assertEquals(0, context.SP);
    }

    @Test
    public void testContextWithNegativeFlags() {
        RunnerContext<Byte> context = new RunnerContext<>((byte) 1, (byte) 2, -1);

        assertEquals(-1, context.flags);
    }

    @Test
    public void testContextWithMaxValues() {
        RunnerContext<Integer> context = new RunnerContext<>(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Arrays.asList(Integer.MAX_VALUE)
        );

        assertEquals(Integer.valueOf(Integer.MAX_VALUE), context.first);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), context.second);
        assertEquals(Integer.MAX_VALUE, context.flags);
        assertEquals(Integer.MAX_VALUE, context.PC);
        assertEquals(Integer.MAX_VALUE, context.SP);
        assertEquals(Integer.MAX_VALUE, context.getRegister(0));
    }

    @Test
    public void testMultipleSwitchesReturnToOriginal() {
        RunnerContext<Integer> original = new RunnerContext<>(10, 20, 0);

        RunnerContext<Integer> switchedOnce = original.switchFirstAndSecond();
        RunnerContext<Integer> switchedTwice = switchedOnce.switchFirstAndSecond();

        assertEquals(original.first, switchedTwice.first);
        assertEquals(original.second, switchedTwice.second);
    }

    @Test
    public void testEmptyRegisters() {
        RunnerContext<Byte> context = new RunnerContext<>((byte) 1, (byte) 2, 0, 0, 0, Collections.emptyList());

        assertTrue(context.registers.isEmpty());
        assertEquals(0, context.registers.size());
    }

    @Test
    public void testLargeRegisterList() {
        List<Integer> registers = Arrays.asList(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
        );
        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0, 0, 0, registers);

        assertEquals(16, context.registers.size());
        for (int i = 0; i < 16; i++) {
            assertEquals(i, context.getRegister(i));
        }
    }

    @Test
    public void testByteOperands() {
        RunnerContext<Byte> context = new RunnerContext<>((byte) -1, (byte) 127, 0);

        assertEquals(Byte.valueOf((byte) -1), context.first);
        assertEquals(Byte.valueOf((byte) 127), context.second);
    }

    @Test
    public void testSwitchWithDifferentOperandTypes() {
        RunnerContext<Byte> context = new RunnerContext<>((byte) 0xFF, (byte) 0x00, 0);
        RunnerContext<Byte> switched = context.switchFirstAndSecond();

        assertEquals(Byte.valueOf((byte) 0x00), switched.first);
        assertEquals(Byte.valueOf((byte) 0xFF), switched.second);
    }
}
