/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.cpu.testsuite.support.SimpleCpuVerifier;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBuilderTest {
    private ByteMemoryStub memory;
    private SimpleCpuRunner cpuRunner;
    private SimpleCpuVerifier cpuVerifier;
    private Builder builder;

    private static class Builder extends TestBuilder<Integer, Builder, SimpleCpuRunner, SimpleCpuVerifier> {
        private Builder(SimpleCpuRunner cpuRunner, SimpleCpuVerifier cpuVerifier) {
            super(cpuRunner, cpuVerifier);
        }
    }

    private static class ByteBuilder extends TestBuilder<Byte, ByteBuilder, SimpleCpuRunner, SimpleCpuVerifier> {
        private ByteBuilder(SimpleCpuRunner cpuRunner, SimpleCpuVerifier cpuVerifier) {
            super(cpuRunner, cpuVerifier);
        }
    }

    @Before
    public void setUp() {
        memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU cpu = mock(CPU.class);
        when(cpu.getAddressSpaceSize()).thenReturn(256);
        cpuRunner = new SimpleCpuRunner(cpu, memory) {
            @Override
            public void step() {
                // Exercise builder mechanics without executing a CPU instruction.
            }
        };
        cpuVerifier = new SimpleCpuVerifier(memory);
        builder = new Builder(cpuRunner, cpuVerifier);
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullRunner() {
        new Builder(null, cpuVerifier);
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullVerifier() {
        new Builder(cpuRunner, null);
    }

    @Test
    public void fixedMemoryInjectorsWriteTheSelectedOperandAndWidth() {
        List<Consumer<Builder>> injectors = List.of(
                b -> b.firstIsMemoryByteAt(0x100), b -> b.secondIsMemoryByteAt(0x100),
                b -> b.firstIsMemoryWordAt(0x100), b -> b.secondIsMemoryWordAt(0x100));
        int[] expected = {0xCD, 0x34, 0xABCD, 0x1234};
        for (int i = 0; i < injectors.size(); i++) {
            setUp();
            cpuRunner.setByte(0x101, 0x5A);
            injectors.get(i).accept(builder);
            builder.run(0).accept(0xABCD, 0x1234);
            assertMemory(0x100, expected[i], i >= 2);
            if (i < 2) {
                cpuVerifier.checkMemoryByte(0x101, 0x5A);
            }
        }
    }

    @Test
    public void operandAddressInjectorsWriteTheConfiguredValue() {
        List<Consumer<Builder>> injectors = List.of(
                b -> b.firstIsMemoryAddressByte(0x42), b -> b.secondIsMemoryAddressByte(0x42),
                b -> b.firstIsMemoryAddressWord(0x1234), b -> b.secondIsMemoryAddressWord(0x1234));
        for (int i = 0; i < injectors.size(); i++) {
            setUp();
            injectors.get(i).accept(builder);
            builder.run(0).accept(0x100, 0x200);
            assertMemory(i % 2 == 0 ? 0x100 : 0x200, i < 2 ? 0x42 : 0x1234, i >= 2);
        }
    }

    @Test
    public void crossOperandInjectorsUseTheCorrectAddressAndValue() {
        List<Consumer<Builder>> injectors = List.of(
                Builder::firstIsAddressAndSecondIsMemoryByte, Builder::secondIsAddressAndFirstIsMemoryByte,
                Builder::firstIsAddressAndSecondIsMemoryWord, Builder::secondIsAddressAndFirstIsMemoryWord);
        for (int i = 0; i < injectors.size(); i++) {
            setUp();
            injectors.get(i).accept(builder);
            builder.run(0).accept(i % 2 == 0 ? 0x100 : 0x1234, i % 2 == 0 ? 0x1234 : 0x100);
            assertMemory(0x100, i < 2 ? 0x34 : 0x1234, i >= 2);
        }
    }

    @Test
    public void byteOperandAddressesAreUnsignedInBothDirections() {
        ByteBuilder byteBuilder = new ByteBuilder(cpuRunner, cpuVerifier);
        byteBuilder.firstIsAddressAndSecondIsMemoryByte().run(0).accept((byte) 0xFF, (byte) 0x42);
        assertMemory(0xFF, 0x42, false);
        byteBuilder.secondIsAddressAndFirstIsMemoryByte().run(0).accept((byte) 0x43, (byte) 0xFE);
        assertMemory(0xFE, 0x43, false);
    }

    @Test
    public void runMethodsAssembleTheSelectedOperandsAndOpcodes() {
        List<TestRunner<SimpleCpuRunner, Integer>> runs = List.of(
                builder.run(0xAA, 0xBB),
                builder.runWithFirstOperand(0xAA),
                builder.runWithSecondOperand(0xAA),
                builder.runWithFirst8bitOperand(0xAA),
                builder.runWithFirst8bitOperandWithOpcodeAfter(0xBB, 0xAA),
                builder.runWithFirst8bitOperandTwoTimes(0xAA),
                builder.runWithBothOperandsWithOpcodeAfter(0xBB, 0xAA));
        int[][] expected = {{0xAA, 0xBB}, {0xAA, 0x34, 0x12}, {0xAA, 0x78, 0x56},
                {0xAA, 0x34}, {0xAA, 0x34, 0xBB}, {0xAA, 0x34, 0x34},
                {0xAA, 0x34, 0x12, 0x78, 0x56, 0xBB}};
        for (int i = 0; i < runs.size(); i++) {
            runs.get(i).accept(0x1234, 0x5678);
            for (int address = 0; address < expected[i].length; address++) {
                cpuVerifier.checkMemoryByte(address, expected[i][address]);
            }
        }
    }

    @Test
    public void explicitExpectationsRemainBoundToTheirPreparedRun() {
        TestRunner<SimpleCpuRunner, Integer> first = builder.firstIsMemoryByteAt(0x100)
                .verifyByte(0x100, context -> context.first).run(0);
        TestRunner<SimpleCpuRunner, Integer> second = builder.secondIsMemoryByteAt(0x200)
                .verifyByte(context -> 0x200, context -> context.second).run(0);
        first.accept(0x42, 0x99);
        second.accept(0x22, 0x55);
        first.accept(0x43, 0x11);
    }

    @Test
    public void wordExpectationUsesItsExplicitAddressAndOperation() {
        builder.firstIsAddressAndSecondIsMemoryWord()
                .verifyWord(context -> context.first, context -> context.second)
                .run(0).accept(0x100, 0x1234);
    }

    @Test(expected = AssertionError.class)
    public void memoryExpectationFailsOnAValueMismatch() {
        builder.firstIsMemoryByteAt(0x100).verifyByte(0x100, context -> context.first + 1)
                .run(0).accept(0x42, 0);
    }

    @Test
    public void runClearsTransientConfigurationAndRetainsSelectedConfiguration() {
        AtomicInteger retained = new AtomicInteger();
        AtomicInteger transientCalls = new AtomicInteger();
        builder.runner.injectNoOperand(runner -> retained.incrementAndGet());
        builder.keepCurrentInjectorsAfterRun();
        builder.runner.injectNoOperand(runner -> transientCalls.incrementAndGet());
        builder.verifyAll(context -> transientCalls.incrementAndGet());
        builder.run(0).accept(0, 0);
        builder.run(0).accept(0, 0);
        assertEquals(2, retained.get());
        assertEquals(2, transientCalls.get());
    }

    @Test
    public void clearAllVerifiersAlsoRemovesRetainedVerifiers() {
        AtomicInteger calls = new AtomicInteger();
        assertSame(builder, builder.verifyAll(context -> calls.incrementAndGet()).keepCurrentVerifiersAfterRun());
        builder.run(0).accept(0, 0);
        builder.clearAllVerifiers().run(0).accept(0, 0);
        assertEquals(1, calls.get());
    }

    @Test
    public void expandMemoryAppliesTheOperandFunction() {
        builder.expandMemory(value -> value.intValue() * 10).run(0).accept(200, 0);
        assertEquals(2000, memory.getSize());
    }

    private void assertMemory(int address, int expected, boolean word) {
        if (word) {
            cpuVerifier.checkMemoryTwoBytes(address, expected);
        } else {
            cpuVerifier.checkMemoryByte(address, expected);
        }
    }
}
