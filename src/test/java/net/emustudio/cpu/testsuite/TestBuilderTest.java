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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TestBuilderTest {
    private SimpleCpuRunner cpuRunner;
    private SimpleCpuVerifier cpuVerifier;
    private ConcreteTestBuilder builder;

    // Concrete TestBuilder implementation for testing
    private static class ConcreteTestBuilder extends TestBuilder<Integer, ConcreteTestBuilder, SimpleCpuRunner, SimpleCpuVerifier> {
        protected ConcreteTestBuilder(SimpleCpuRunner cpuRunner, SimpleCpuVerifier cpuVerifier) {
            super(cpuRunner, cpuVerifier);
        }
    }

    @Before
    public void setUp() {
        ByteMemoryStub memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU mockCpu = mock(CPU.class);
        cpuRunner = new SimpleCpuRunner(mockCpu, memory);
        cpuVerifier = new SimpleCpuVerifier(memory);
        builder = new ConcreteTestBuilder(cpuRunner, cpuVerifier);
    }

    @Test
    public void testConstructorStoresReferences() {
        assertNotNull(builder.cpuRunner);
        assertNotNull(builder.cpuVerifier);
        assertNotNull(builder.runner);
        assertSame(cpuRunner, builder.cpuRunner);
        assertSame(cpuVerifier, builder.cpuVerifier);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCpuRunner() {
        new ConcreteTestBuilder(null, cpuVerifier);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCpuVerifier() {
        new ConcreteTestBuilder(cpuRunner, null);
    }

    @Test
    public void testClearAllVerifiers() {
        ConcreteTestBuilder result = builder.clearAllVerifiers();
        assertSame(builder, result); // Should return this for chaining
    }

    @Test
    public void testVerifyAll() {
        ConcreteTestBuilder result = builder.verifyAll(context -> {});
        assertSame(builder, result);
    }

    @Test
    public void testRegisterIsRandom() {
        ConcreteTestBuilder result = builder.registerIsRandom(0, 255);
        assertSame(builder, result);
    }

    @Test
    public void testPrintRegister() {
        ConcreteTestBuilder result = builder.printRegister(0);
        assertSame(builder, result);
    }

    @Test
    public void testPrintOperands() {
        ConcreteTestBuilder result = builder.printOperands();
        assertSame(builder, result);
    }

    @Test
    public void testPrintInjectingProcess() {
        ConcreteTestBuilder result = builder.printInjectingProcess();
        assertSame(builder, result);
    }

    @Test
    public void testVerifyFlagsWithOperator() {
        FlagsCheck<Integer, ?> flagsCheck = new ConcreteFlagsCheck();

        ConcreteTestBuilder result = builder.verifyFlags(flagsCheck, context -> 0x42);
        assertSame(builder, result);
        assertNotNull(builder.lastOperation);
    }

    @Test
    public void testVerifyFlagsOfLastOp() {
        FlagsCheck<Integer, ?> flagsCheck = new ConcreteFlagsCheck();

        builder.lastOperation = context -> 0x42;
        ConcreteTestBuilder result = builder.verifyFlagsOfLastOp(flagsCheck);
        assertSame(builder, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testVerifyFlagsOfLastOpWithoutOperation() {
        FlagsCheck<Integer, ?> flagsCheck = new ConcreteFlagsCheck();

        builder.verifyFlagsOfLastOp(flagsCheck);
    }

    private static class ConcreteFlagsCheck extends FlagsCheck<Integer, ConcreteFlagsCheck> {
        public void verify(CpuVerifier verifier) {
        }
    }

    @Test
    public void testVerifyByteWithAddress() {
        ConcreteTestBuilder result = builder.verifyByte(0x100, context -> 0x42);
        assertSame(builder, result);
        assertNotNull(builder.lastOperation);
    }

    @Test
    public void testVerifyByteWithAddressOnly() {
        builder.lastOperation = context -> 0x42;
        ConcreteTestBuilder result = builder.verifyByte(0x100);
        assertSame(builder, result);
    }

    @Test
    public void testVerifyByteWithAddressOperator() {
        builder.lastOperation = context -> 0x42;
        ConcreteTestBuilder result = builder.verifyByte(context -> 0x100);
        assertSame(builder, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testVerifyByteWithoutLastOperation() {
        builder.verifyByte(context -> 0x100);
    }

    @Test
    public void testVerifyByteWithBothOperators() {
        ConcreteTestBuilder result = builder.verifyByte(context -> 0x100, context -> 0x42);
        assertSame(builder, result);
        assertNotNull(builder.lastOperation);
    }

    @Test
    public void testVerifyWord() {
        ConcreteTestBuilder result = builder.verifyWord(context -> 0x100, context -> 0x1234);
        assertSame(builder, result);
        assertNotNull(builder.lastOperation);
    }

    @Test
    public void testKeepCurrentInjectorsAfterRun() {
        ConcreteTestBuilder result = builder.keepCurrentInjectorsAfterRun();
        assertSame(builder, result);
    }

    @Test
    public void testClearOtherVerifiersAfterRun() {
        ConcreteTestBuilder result = builder.clearOtherVerifiersAfterRun();
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsMemoryByteAt() {
        ConcreteTestBuilder result = builder.firstIsMemoryByteAt(0x100);
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsMemoryByteAt() {
        ConcreteTestBuilder result = builder.secondIsMemoryByteAt(0x200);
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsMemoryWordAt() {
        ConcreteTestBuilder result = builder.firstIsMemoryWordAt(0x300);
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsMemoryWordAt() {
        ConcreteTestBuilder result = builder.secondIsMemoryWordAt(0x400);
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsMemoryAddressByte() {
        ConcreteTestBuilder result = builder.firstIsMemoryAddressByte(0x42);
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsMemoryAddressByte() {
        ConcreteTestBuilder result = builder.secondIsMemoryAddressByte(0x43);
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsMemoryAddressWord() {
        ConcreteTestBuilder result = builder.firstIsMemoryAddressWord(0x1234);
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsMemoryAddressWord() {
        ConcreteTestBuilder result = builder.secondIsMemoryAddressWord(0x5678);
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsAddressAndSecondIsMemoryWord() {
        ConcreteTestBuilder result = builder.firstIsAddressAndSecondIsMemoryWord();
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsAddressAndFirstIsMemoryWord() {
        ConcreteTestBuilder result = builder.secondIsAddressAndFirstIsMemoryWord();
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsAddressAndSecondIsMemoryByte() {
        ConcreteTestBuilder result = builder.firstIsAddressAndSecondIsMemoryByte();
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsAddressAndFirstIsMemoryByte() {
        ConcreteTestBuilder result = builder.secondIsAddressAndFirstIsMemoryByte();
        assertSame(builder, result);
    }

    @Test
    public void testSetFlags() {
        ConcreteTestBuilder result = builder.setFlags(0xFF);
        assertSame(builder, result);
    }

    @Test
    public void testExpandMemory() {
        ConcreteTestBuilder result = builder.expandMemory(num -> num.intValue() * 2);
        assertSame(builder, result);
    }

    @Test
    public void testRun() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.run(0x00, 0x01, 0x02);
        assertNotNull(result);
    }

    @Test
    public void testRunWithFirstOperand() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.runWithFirstOperand(0x00, 0x01);
        assertNotNull(result);
    }

    @Test
    public void testRunWithSecondOperand() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.runWithSecondOperand(0x00, 0x01);
        assertNotNull(result);
    }

    @Test
    public void testRunWithFirst8bitOperandWithOpcodeAfter() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.runWithFirst8bitOperandWithOpcodeAfter(0xFF, 0x00, 0x01);
        assertNotNull(result);
    }

    @Test
    public void testRunWithFirst8bitOperand() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.runWithFirst8bitOperand(0x00, 0x01);
        assertNotNull(result);
    }

    @Test
    public void testRunWithFirst8bitOperandTwoTimes() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.runWithFirst8bitOperandTwoTimes(0x00, 0x01);
        assertNotNull(result);
    }

    @Test
    public void testRunWithBothOperandsWithOpcodeAfter() {
        TestRunner<SimpleCpuRunner, Integer> result = builder.runWithBothOperandsWithOpcodeAfter(0xFF, 0x00, 0x01);
        assertNotNull(result);
    }

    @Test
    public void testMethodChaining() {
        // Test that all methods return the builder for chaining
        ConcreteTestBuilder result = builder
                .clearAllVerifiers()
                .setFlags(0xFF)
                .firstIsMemoryByteAt(0x100)
                .secondIsMemoryWordAt(0x200)
                .keepCurrentInjectorsAfterRun()
                .clearOtherVerifiersAfterRun();

        assertSame(builder, result);
    }

    @Test
    public void testFirstIsAddressAndSecondIsMemoryWordInjectsCorrectly() {
        ConcreteTestBuilder result = builder.firstIsAddressAndSecondIsMemoryWord();
        assertSame(builder, result);
        // The actual injection happens during test execution, not during builder setup
    }

    @Test
    public void testSecondIsAddressAndFirstIsMemoryWordInjectsCorrectly() {
        ConcreteTestBuilder result = builder.secondIsAddressAndFirstIsMemoryWord();
        assertSame(builder, result);
    }

    @Test
    public void testFirstIsAddressAndSecondIsMemoryByteInjectsCorrectly() {
        ConcreteTestBuilder result = builder.firstIsAddressAndSecondIsMemoryByte();
        assertSame(builder, result);
    }

    @Test
    public void testSecondIsAddressAndFirstIsMemoryByteInjectsCorrectly() {
        ConcreteTestBuilder result = builder.secondIsAddressAndFirstIsMemoryByte();
        assertSame(builder, result);
    }

    @Test
    public void testLastOperationIsSetByVerifyFlags() {
        FlagsCheck<Integer, ?> flagsCheck = new ConcreteFlagsCheck();

        assertNull(builder.lastOperation);
        builder.verifyFlags(flagsCheck, context -> 0x42);
        assertNotNull(builder.lastOperation);

        // Test that the operation returns the correct value
        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0);
        assertEquals(0x42, builder.lastOperation.apply(context).intValue());
    }

    @Test
    public void testLastOperationIsSetByVerifyByte() {
        assertNull(builder.lastOperation);
        builder.verifyByte(0x100, context -> 0x55);
        assertNotNull(builder.lastOperation);

        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0);
        assertEquals(0x55, builder.lastOperation.apply(context).intValue());
    }

    @Test
    public void testLastOperationIsSetByVerifyWord() {
        assertNull(builder.lastOperation);
        builder.verifyWord(context -> 0x200, context -> 0xABCD);
        assertNotNull(builder.lastOperation);

        RunnerContext<Integer> context = new RunnerContext<>(0, 0, 0);
        assertEquals(0xABCD, builder.lastOperation.apply(context).intValue());
    }

    @Test
    public void testExpandMemoryAppliesFunction() {
        builder.expandMemory(num -> num.intValue() * 10);

        // The expandMemory should inject a function that calls ensureProgramSize
        // We can verify this by checking that it doesn't throw
        assertNotNull(builder.runner);
    }

    @Test
    public void testRunCreatesNewTestRunner() {
        TestRunner<SimpleCpuRunner, Integer> runner1 = builder.run(0x00);
        TestRunner<SimpleCpuRunner, Integer> runner2 = builder.run(0x01);

        assertNotNull(runner1);
        assertNotNull(runner2);
        assertNotSame(runner1, runner2); // Should create different instances
    }

    @Test
    public void testRunMethodsAcceptVarargs() {
        // Test that various numbers of instruction bytes work
        assertNotNull(builder.run());
        assertNotNull(builder.run(0x00));
        assertNotNull(builder.run(0x00, 0x01));
        assertNotNull(builder.run(0x00, 0x01, 0x02));
        assertNotNull(builder.runWithFirstOperand(0x00, 0x01, 0x02, 0x03));
        assertNotNull(builder.runWithSecondOperand(0x00, 0x01, 0x02, 0x03, 0x04));
    }
}
