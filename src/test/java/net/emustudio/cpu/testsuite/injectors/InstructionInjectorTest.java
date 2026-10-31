/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class InstructionInjectorTest {
    @Parameterized.Parameters(name = "operand={0}, requiredLength={1}")
    public static Collection<Object[]> operands() {
        return Arrays.asList(new Object[][] {
                {(byte) 0x00, 16}, {(byte) 0x80, 130}, {(byte) 0xFF, 257},
                {0x1000, 4098}, {0xFFFF, 65537}, {0xABCD1000, 4098}, {-1, 65537}
        });
    }

    private final Number operand;
    private final int requiredLength;
    private ByteMemoryStub memory;
    private SimpleCpuRunner runner;

    public InstructionInjectorTest(Number operand, int requiredLength) {
        this.operand = operand;
        this.requiredLength = requiredLength;
    }

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU cpu = mock(CPU.class);
        when(cpu.getAddressSpaceSize()).thenReturn(16);
        runner = new SimpleCpuRunner(cpu, memory);
    }

    @Test
    public void oneOperandCoversAddressAndPreservesInstruction() {
        new OneOperInstr<SimpleCpuRunner, Number>(0x01).accept(runner, operand);
        assertEquals(requiredLength, memory.getSize());
        assertEquals(0x01, memory.read(0).intValue() & 0xFF);
        assertEquals(operand.intValue() & 0xFF, memory.read(1).intValue() & 0xFF);
    }

    @Test
    public void twoOperandsCoverFirstAddress() {
        new TwoOperInstr<SimpleCpuRunner, Number>(0x01).inject(runner, operand, (byte) 0);
        assertEquals(requiredLength, memory.getSize());
        assertEquals(0x01, memory.read(0).intValue() & 0xFF);
        assertEquals(operand.intValue() & 0xFF, memory.read(1).intValue() & 0xFF);
    }

    @Test
    public void twoOperandsCoverSecondAddress() {
        new TwoOperInstr<SimpleCpuRunner, Number>(0x01).inject(runner, (byte) 0, operand);
        assertEquals(requiredLength, memory.getSize());
        assertEquals(0x01, memory.read(0).intValue() & 0xFF);
        assertEquals(operand.intValue() & 0xFF, memory.read(2).intValue() & 0xFF);
    }
}
