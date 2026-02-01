/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.injectors.TwoOperInjector;
import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestRunnerTest {
    private SimpleCpuRunner cpuRunner;
    private TestRunner<SimpleCpuRunner, Integer> testRunner;
    private CPU.CPUListener cpuListener;

    @Before
    public void setUp() {
        ByteMemoryStub memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        CPU mockCpu = mock(CPU.class);

        // Capture the CPU listener so we can simulate state changes
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                cpuListener = invocation.getArgument(0);
                return null;
            }
        }).when(mockCpu).addCPUListener(any(CPU.CPUListener.class));

        // Mock CPU.step() to call the listener with STATE_STOPPED_BREAK
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                // Simulate CPU calling the listener when step completes
                if (cpuListener != null) {
                    cpuListener.runStateChanged(CPU.RunState.STATE_STOPPED_BREAK);
                }
                return null;
            }
        }).when(mockCpu).step();

        cpuRunner = spy(new SimpleCpuRunner(mockCpu, memory));
        testRunner = new TestRunner<>(cpuRunner);
    }

    @Test
    public void testConstructorStoresCpuRunner() {
        assertNotNull(testRunner);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCpuRunner() {
        new TestRunner<SimpleCpuRunner, Integer>(null);
    }

    @Test
    public void testInjectNoOperand() {
        AtomicBoolean injectorCalled = new AtomicBoolean(false);
        Consumer<SimpleCpuRunner> injector = runner -> injectorCalled.set(true);

        TestRunner<SimpleCpuRunner, Integer> result = testRunner.injectNoOperand(injector);

        assertSame(testRunner, result); // Should return this for chaining
        testRunner.accept(10, 20);
        assertTrue(injectorCalled.get());
    }

    @Test
    public void testInjectNoOperandWithMultipleInjectors() {
        AtomicInteger callCount = new AtomicInteger(0);
        Consumer<SimpleCpuRunner> injector1 = runner -> callCount.incrementAndGet();
        Consumer<SimpleCpuRunner> injector2 = runner -> callCount.incrementAndGet();
        Consumer<SimpleCpuRunner> injector3 = runner -> callCount.incrementAndGet();

        testRunner.injectNoOperand(injector1, injector2, injector3);
        testRunner.accept(0, 0);

        assertEquals(3, callCount.get());
    }

    @Test
    public void testInjectFirstOperand() {
        AtomicInteger capturedOperand = new AtomicInteger(0);
        BiConsumer<SimpleCpuRunner, Integer> injector = (runner, operand) -> capturedOperand.set(operand);

        TestRunner<SimpleCpuRunner, Integer> result = testRunner.injectFirst(injector);

        assertSame(testRunner, result);
        testRunner.accept(42, 99);
        assertEquals(42, capturedOperand.get());
    }

    @Test
    public void testInjectFirstWithMultipleInjectors() {
        List<Integer> capturedValues = new ArrayList<>();
        BiConsumer<SimpleCpuRunner, Integer> injector1 = (runner, operand) -> capturedValues.add(operand);
        BiConsumer<SimpleCpuRunner, Integer> injector2 = (runner, operand) -> capturedValues.add(operand * 2);

        testRunner.injectFirst(injector1, injector2);
        testRunner.accept(5, 10);

        assertEquals(Arrays.asList(5, 10), capturedValues);
    }

    @Test
    public void testInjectSecondOperand() {
        AtomicInteger capturedOperand = new AtomicInteger(0);
        BiConsumer<SimpleCpuRunner, Integer> injector = (runner, operand) -> capturedOperand.set(operand);

        TestRunner<SimpleCpuRunner, Integer> result = testRunner.injectSecond(injector);

        assertSame(testRunner, result);
        testRunner.accept(42, 99);
        assertEquals(99, capturedOperand.get());
    }

    @Test
    public void testInjectSecondWithMultipleInjectors() {
        List<Integer> capturedValues = new ArrayList<>();
        BiConsumer<SimpleCpuRunner, Integer> injector1 = (runner, operand) -> capturedValues.add(operand);
        BiConsumer<SimpleCpuRunner, Integer> injector2 = (runner, operand) -> capturedValues.add(operand * 3);

        testRunner.injectSecond(injector1, injector2);
        testRunner.accept(5, 7);

        assertEquals(Arrays.asList(7, 21), capturedValues);
    }

    @Test
    public void testInjectTwoOperands() {
        AtomicInteger firstOperand = new AtomicInteger(0);
        AtomicInteger secondOperand = new AtomicInteger(0);
        TwoOperInjector<SimpleCpuRunner, Integer> injector = (runner, first, second) -> {
            firstOperand.set(first);
            secondOperand.set(second);
        };

        TestRunner<SimpleCpuRunner, Integer> result = testRunner.injectTwoOperands(injector);

        assertSame(testRunner, result);
        testRunner.accept(11, 22);
        assertEquals(11, firstOperand.get());
        assertEquals(22, secondOperand.get());
    }

    @Test
    public void testInjectTwoOperandsWithMultipleInjectors() {
        List<Integer> capturedValues = new ArrayList<>();
        TwoOperInjector<SimpleCpuRunner, Integer> injector1 = (runner, first, second) -> {
            capturedValues.add(first);
            capturedValues.add(second);
        };
        TwoOperInjector<SimpleCpuRunner, Integer> injector2 = (runner, first, second) -> {
            capturedValues.add(first + second);
        };

        testRunner.injectTwoOperands(injector1, injector2);
        testRunner.accept(3, 4);

        assertEquals(Arrays.asList(3, 4, 7), capturedValues);
    }

    @Test
    public void testVerifyAfterTest() {
        AtomicBoolean verifierCalled = new AtomicBoolean(false);
        Consumer<RunnerContext<Integer>> verifier = context -> verifierCalled.set(true);

        testRunner.verifyAfterTest(verifier);
        testRunner.accept(1, 2);

        assertTrue(verifierCalled.get());
    }

    @Test
    public void testVerifyAfterTestWithMultipleVerifiers() {
        AtomicInteger verifierCount = new AtomicInteger(0);
        Consumer<RunnerContext<Integer>> verifier1 = context -> verifierCount.incrementAndGet();
        Consumer<RunnerContext<Integer>> verifier2 = context -> verifierCount.incrementAndGet();
        Consumer<RunnerContext<Integer>> verifier3 = context -> verifierCount.incrementAndGet();

        testRunner.verifyAfterTest(verifier1, verifier2, verifier3);
        testRunner.accept(0, 0);

        assertEquals(3, verifierCount.get());
    }

    @Test
    public void testVerifierReceivesCorrectContext() {
        AtomicInteger capturedFirst = new AtomicInteger(0);
        AtomicInteger capturedSecond = new AtomicInteger(0);
        Consumer<RunnerContext<Integer>> verifier = context -> {
            capturedFirst.set(context.first);
            capturedSecond.set(context.second);
        };

        testRunner.verifyAfterTest(verifier);
        testRunner.accept(15, 25);

        assertEquals(15, capturedFirst.get());
        assertEquals(25, capturedSecond.get());
    }

    @Test
    public void testAcceptCallsResetOnCpuRunner() {
        testRunner.accept(1, 2);
        verify(cpuRunner, times(1)).reset();
    }

    @Test
    public void testAcceptCallsStepOnCpuRunner() {
        testRunner.accept(1, 2);
        verify(cpuRunner, times(1)).step();
    }

    @Test
    public void testAcceptCallsInjectorsBeforeStep() {
        AtomicBoolean injectorCalled = new AtomicBoolean(false);
        AtomicBoolean stepCalledAfterInjector = new AtomicBoolean(false);

        testRunner.injectNoOperand(runner -> {
            injectorCalled.set(true);
            verify(cpuRunner, never()).step(); // Step should not be called yet
        });

        doAnswer(invocation -> {
            stepCalledAfterInjector.set(injectorCalled.get());
            return null;
        }).when(cpuRunner).step();

        testRunner.accept(1, 2);

        assertTrue(injectorCalled.get());
        assertTrue(stepCalledAfterInjector.get());
    }

    @Test
    public void testAcceptCallsVerifiersAfterStep() {
        AtomicBoolean verifierCalled = new AtomicBoolean(false);
        AtomicBoolean stepCalledBeforeVerifier = new AtomicBoolean(false);

        doAnswer(invocation -> {
            verify(cpuRunner, times(1)).reset();
            return null;
        }).when(cpuRunner).step();

        testRunner.verifyAfterTest(context -> {
            verifierCalled.set(true);
            verify(cpuRunner, times(1)).step(); // Step should be called before verifier
        });

        testRunner.accept(1, 2);
        assertTrue(verifierCalled.get());
    }

    @Test
    public void testClearInjectors() {
        AtomicInteger callCount = new AtomicInteger(0);
        testRunner.injectNoOperand(runner -> callCount.incrementAndGet());

        testRunner.accept(0, 0);
        assertEquals(1, callCount.get());

        testRunner.clearInjectors();
        testRunner.accept(0, 0);
        assertEquals(1, callCount.get()); // Should not be called again
    }

    @Test
    public void testClearVerifiers() {
        AtomicInteger callCount = new AtomicInteger(0);
        testRunner.verifyAfterTest(context -> callCount.incrementAndGet());

        testRunner.accept(0, 0);
        assertEquals(1, callCount.get());

        testRunner.clearVerifiers();
        testRunner.accept(0, 0);
        assertEquals(1, callCount.get()); // Should not be called again
    }

    @Test
    public void testClearAllVerifiers() {
        AtomicInteger callCount = new AtomicInteger(0);
        Consumer<RunnerContext<Integer>> verifier = context -> callCount.incrementAndGet();

        testRunner.verifyAfterTest(verifier);
        testRunner.keepCurrentVerifiersAfterClear();

        testRunner.accept(0, 0);
        assertEquals(1, callCount.get());

        testRunner.clearVerifiers();
        testRunner.accept(0, 0);
        assertEquals(2, callCount.get()); // Still called because it was kept

        testRunner.clearAllVerifiers();
        testRunner.accept(0, 0);
        assertEquals(2, callCount.get()); // Not called anymore
    }

    @Test
    public void testKeepCurrentInjectorsAfterClear() {
        AtomicInteger callCount = new AtomicInteger(0);
        testRunner.injectNoOperand(runner -> callCount.incrementAndGet());
        testRunner.keepCurrentInjectorsAfterClear();

        testRunner.accept(0, 0);
        assertEquals(1, callCount.get());

        testRunner.clearInjectors();
        testRunner.accept(0, 0);
        assertEquals(2, callCount.get()); // Should still be called
    }

    @Test
    public void testKeepCurrentVerifiersAfterClear() {
        AtomicInteger callCount = new AtomicInteger(0);
        testRunner.verifyAfterTest(context -> callCount.incrementAndGet());
        testRunner.keepCurrentVerifiersAfterClear();

        testRunner.accept(0, 0);
        assertEquals(1, callCount.get());

        testRunner.clearVerifiers();
        testRunner.accept(0, 0);
        assertEquals(2, callCount.get()); // Should still be called
    }

    @Test
    public void testPrintInjectingProcess() {
        // Just verify it doesn't throw an exception
        testRunner.printInjectingProcess();
        testRunner.injectFirst((runner, operand) -> {});
        testRunner.accept(1, 2);
    }

    @Test
    public void testClone() {
        testRunner.injectNoOperand(runner -> {});
        testRunner.verifyAfterTest(context -> {});

        TestRunner<SimpleCpuRunner, Integer> cloned = testRunner.clone();

        assertNotNull(cloned);
        assertNotSame(testRunner, cloned);
    }

    @Test
    public void testClonedRunnerIsIndependent() {
        AtomicInteger originalCount = new AtomicInteger(0);
        AtomicInteger clonedCount = new AtomicInteger(0);

        testRunner.injectNoOperand(runner -> originalCount.incrementAndGet());
        TestRunner<SimpleCpuRunner, Integer> cloned = testRunner.clone();
        cloned.injectNoOperand(runner -> clonedCount.incrementAndGet());

        testRunner.accept(0, 0);
        assertEquals(1, originalCount.get());
        assertEquals(0, clonedCount.get()); // Cloned injector not executed on original

        cloned.accept(0, 0);
        assertEquals(2, originalCount.get()); // Original injector copied to clone
        assertEquals(1, clonedCount.get());
    }

    @Test
    public void testClonePreservesKeptInjectors() {
        AtomicInteger callCount = new AtomicInteger(0);
        testRunner.injectNoOperand(runner -> callCount.incrementAndGet());
        testRunner.keepCurrentInjectorsAfterClear();

        TestRunner<SimpleCpuRunner, Integer> cloned = testRunner.clone();
        cloned.clearInjectors();
        cloned.accept(0, 0);

        assertEquals(1, callCount.get()); // Should still be called in cloned runner
    }

    @Test
    public void testClonePreservesKeptVerifiers() {
        AtomicInteger callCount = new AtomicInteger(0);
        testRunner.verifyAfterTest(context -> callCount.incrementAndGet());
        testRunner.keepCurrentVerifiersAfterClear();

        TestRunner<SimpleCpuRunner, Integer> cloned = testRunner.clone();
        cloned.clearVerifiers();
        cloned.accept(0, 0);

        assertEquals(1, callCount.get()); // Should still be called in cloned runner
    }

    @Test
    public void testMultipleAcceptCallsWithSameRunner() {
        AtomicInteger injectorCallCount = new AtomicInteger(0);
        AtomicInteger verifierCallCount = new AtomicInteger(0);

        testRunner.injectNoOperand(runner -> injectorCallCount.incrementAndGet());
        testRunner.verifyAfterTest(context -> verifierCallCount.incrementAndGet());

        testRunner.accept(1, 2);
        testRunner.accept(3, 4);
        testRunner.accept(5, 6);

        assertEquals(3, injectorCallCount.get());
        assertEquals(3, verifierCallCount.get());
        verify(cpuRunner, times(3)).reset();
        verify(cpuRunner, times(3)).step();
    }

    @Test
    public void testInjectorsExecuteInOrder() {
        List<Integer> executionOrder = new ArrayList<>();
        testRunner.injectNoOperand(runner -> executionOrder.add(1));
        testRunner.injectFirst((runner, operand) -> executionOrder.add(2));
        testRunner.injectSecond((runner, operand) -> executionOrder.add(3));
        testRunner.injectTwoOperands((runner, first, second) -> executionOrder.add(4));

        testRunner.accept(0, 0);

        assertEquals(Arrays.asList(1, 2, 3, 4), executionOrder);
    }

    @Test
    public void testVerifiersExecuteInOrder() {
        List<Integer> executionOrder = new ArrayList<>();
        testRunner.verifyAfterTest(context -> executionOrder.add(1));
        testRunner.verifyAfterTest(context -> executionOrder.add(2));
        testRunner.verifyAfterTest(context -> executionOrder.add(3));

        testRunner.accept(0, 0);

        assertEquals(Arrays.asList(1, 2, 3), executionOrder);
    }

    @Test
    public void testChainedInjectorCalls() {
        AtomicInteger count = new AtomicInteger(0);

        TestRunner<SimpleCpuRunner, Integer> result = testRunner
                .injectNoOperand(r -> count.incrementAndGet())
                .injectFirst((r, o) -> count.incrementAndGet())
                .injectSecond((r, o) -> count.incrementAndGet())
                .injectTwoOperands((r, f, s) -> count.incrementAndGet());

        assertSame(testRunner, result);
        testRunner.accept(0, 0);
        assertEquals(4, count.get());
    }

    @Test
    public void testContextContainsCorrectOperands() {
        List<Integer> capturedOperands = new ArrayList<>();
        testRunner.verifyAfterTest(context -> {
            capturedOperands.add(context.first);
            capturedOperands.add(context.second);
        });

        testRunner.accept(123, 456);

        assertEquals(Arrays.asList(123, 456), capturedOperands);
    }
}
