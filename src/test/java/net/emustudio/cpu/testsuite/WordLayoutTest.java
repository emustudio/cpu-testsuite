/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.injectors.MemoryWord;
import net.emustudio.cpu.testsuite.injectors.MemoryAddress;
import net.emustudio.cpu.testsuite.injectors.OneOperInstr;
import net.emustudio.cpu.testsuite.injectors.TwoOperInstr;
import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.memory.MemoryStub;
import net.emustudio.cpu.testsuite.memory.ShortMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.cpu.testsuite.support.SimpleCpuVerifier;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils.Strategy;
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
public class WordLayoutTest {
    @Parameterized.Parameters(name = "strategy={0}, byteCells={1}")
    public static Collection<Object[]> layouts() {
        return Arrays.asList(new Object[][] {
                {Strategy.LITTLE_ENDIAN, true, 0x34, 0x12},
                {Strategy.BIG_ENDIAN, true, 0x12, 0x34},
                {Strategy.LITTLE_ENDIAN | Strategy.REVERSE_BITS, true, 0x2C, 0x48},
                {Strategy.BIG_ENDIAN | Strategy.REVERSE_BITS, true, 0x48, 0x2C},
                {Strategy.LITTLE_ENDIAN, false, 0x34, 0x12},
                {Strategy.BIG_ENDIAN, false, 0x12, 0x34},
                {Strategy.LITTLE_ENDIAN | Strategy.REVERSE_BITS, false, 0x2C, 0x48},
                {Strategy.BIG_ENDIAN | Strategy.REVERSE_BITS, false, 0x48, 0x2C}
        });
    }

    private final int strategy;
    private final boolean byteCells;
    private final int firstByte;
    private final int secondByte;
    private MemoryStub<?> memory;
    private SimpleCpuRunner runner;
    private SimpleCpuVerifier verifier;

    public WordLayoutTest(int strategy, boolean byteCells, int firstByte, int secondByte) {
        this.strategy = strategy;
        this.byteCells = byteCells;
        this.firstByte = firstByte;
        this.secondByte = secondByte;
    }

    @Before
    public void setUp() {
        memory = byteCells ? new ByteMemoryStub(strategy) : new ShortMemoryStub(strategy);
        CPU cpu = mock(CPU.class);
        when(cpu.getAddressSpaceSize()).thenReturn(1024);
        runner = new SimpleCpuRunner(cpu, memory) {
            @Override
            public void step() {
                // The CPU instruction is irrelevant to checking injected memory.
            }
        };
        verifier = new SimpleCpuVerifier(memory);
    }

    @Test
    public void memoryWordInjectorUsesConfiguredLayoutAndTruncates() {
        new MemoryWord<SimpleCpuRunner, Integer>(0x100).accept(runner, 0xABCD1234);
        assertWord(0x100);
    }

    @Test
    public void operandAddressWordInjectorUsesConfiguredLayoutAndTruncates() {
        new MemoryAddress<SimpleCpuRunner, Integer>(0xABCD1234).accept(runner, 0x100);
        assertWord(0x100);
    }

    @Test
    public void builderWordValueAtFirstOperandAddressUsesConfiguredLayout() {
        new Builder(runner, verifier).firstIsMemoryAddressWord(0x1234).run(0x00).accept(0x100, 0);
        assertWord(0x100);
    }

    @Test
    public void builderWordValueAtSecondOperandAddressUsesConfiguredLayout() {
        new Builder(runner, verifier).secondIsMemoryAddressWord(0x1234).run(0x00).accept(0, 0x100);
        assertWord(0x100);
    }

    @Test
    public void builderFixedWordAddressUsesConfiguredLayout() {
        new Builder(runner, verifier).firstIsMemoryWordAt(0x100).run(0x00).accept(0x1234, 0);
        assertWord(0x100);
    }

    @Test
    public void builderFirstOperandAddressUsesConfiguredLayout() {
        new Builder(runner, verifier).firstIsAddressAndSecondIsMemoryWord().run(0x00).accept(0x100, 0x1234);
        assertWord(0x100);
    }

    @Test
    public void builderSecondOperandAddressUsesConfiguredLayout() {
        new Builder(runner, verifier).secondIsAddressAndFirstIsMemoryWord().run(0x00).accept(0x1234, 0x100);
        assertWord(0x100);
    }

    @Test
    public void oneOperandInstructionUsesConfiguredLayout() {
        new OneOperInstr<SimpleCpuRunner, Integer>(0x01).accept(runner, 0x1234);
        assertEquals(0x01, memory.read(0).intValue());
        assertWord(1);
    }

    @Test
    public void twoOperandInstructionUsesConfiguredLayout() {
        new TwoOperInstr<SimpleCpuRunner, Integer>(0x01).inject(runner, 0x1234, 0x1234);
        assertWord(1);
        assertWord(3);
    }

    @Test
    public void byteOperandRemainsOneUnmodifiedByte() {
        new OneOperInstr<SimpleCpuRunner, Byte>(0x01).accept(runner, (byte) 0x92);
        assertEquals(0x92, memory.read(1).intValue() & 0xFF);
        assertEquals(0, memory.read(2).intValue());
    }

    private void assertWord(int address) {
        assertEquals(firstByte, memory.read(address).intValue() & 0xFF);
        assertEquals(secondByte, memory.read(address + 1).intValue() & 0xFF);
        verifier.checkMemoryTwoBytes(address, 0x1234);
    }

    private static class Builder extends TestBuilder<Integer, Builder, SimpleCpuRunner, SimpleCpuVerifier> {
        private Builder(SimpleCpuRunner runner, SimpleCpuVerifier verifier) {
            super(runner, verifier);
        }
    }
}
