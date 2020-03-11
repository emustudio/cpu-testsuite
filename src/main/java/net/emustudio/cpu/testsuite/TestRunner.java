/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.injectors.TwoOperInjector;
import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Test runner/executor.
 *
 * At first, the test is prepared by injecting the operands into all provided injectors. The injectors know
 * what to do. They are using CpuRunner class for manipulating (preparing) CPU state. They are provided by user.
 *
 * Then, the test is executed (by executing cpuRunner.step()). Flags are saved for further execution.
 *
 * After, the resulting CPU state should be verified by all provided verifiers. Again, all verifiers know
 * what to do. They are using CpuVerifier class for checking the CPU state. They are provided by user.
 *
 * @param <TCpuRunner> CPU Runner type
 * @param <TOperand> operands type (Byte or Integer)
 */
@NotThreadSafe
public class TestRunner<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TOperand, TOperand> {
    private final TCpuRunner cpuRunner;

    private final List<TwoOperInjector<TCpuRunner, TOperand>> injectors = new ArrayList<>();
    private final List<TwoOperInjector<TCpuRunner, TOperand>> injectorsToKeep = new ArrayList<>();

    private final List<Consumer<RunnerContext<TOperand>>> verifiers = new ArrayList<>();
    private final List<Consumer<RunnerContext<TOperand>>> verifiersToKeep = new ArrayList<>();

    private int flagsBefore = -1;
    private boolean printInjectingProcess;

    /**
     * Creates new TestRunner.
     *
     * @param cpuRunner CPU runner object (for manipulating CPU state).
     */
    public TestRunner(TCpuRunner cpuRunner) {
        this.cpuRunner = Objects.requireNonNull(cpuRunner);
    }

    /**
     * Keep currently set injectors after test is executed.
     *
     * Each test execution will clear all injectors, because sometimes they are based on specific conditions of single
     * test instance. By using this method, current injectors will be used also in another test executions.
     *
     * Injectors added after calling this method will not be preserved, again.
     */
    public void keepCurrentInjectorsAfterClear() {
        injectorsToKeep.addAll(injectors);
    }

    /**
     * Keep currently set verifiers after test is executed.
     *
     * Each test execution will clear all verifiers, because sometimes they are based on specific conditions of single
     * test instance. By using this method, current verifiers will be used also in another test executions.
     *
     * Verifiers added after calling this method will not be preserved, again.
     */
    public void keepCurrentVerifiersAfterClear() {
        verifiersToKeep.addAll(verifiers);
    }


    /**
     * Print the process of injecting the operands into injectors. Useful for debugging.
     */
    public void printInjectingProcess() {
        this.printInjectingProcess = true;
    }

    /**
     * Inject a CpuRunner to all specified injectors when operands are not needed.
     *
     * For example, it might be useful when injecting an instruction with no operands.
     *
     * @param injectors injectors requiring only CpuRunner (no operands)
     * @return this
     */
    @SafeVarargs
    public final TestRunner<TCpuRunner, TOperand> injectNoOperand(Consumer<TCpuRunner>... injectors) {
        for (Consumer<TCpuRunner> injector : injectors) {
            this.injectors.add((cpuRunner, first, second) -> {
                printInjectionIfEnabled("", injector);
                injector.accept(cpuRunner);
            });
        }
        return this;
    }

    /**
     * Inject a CpuRunner and single operand into specified injectors.
     *
     * RunnerContext has always two operands. In this case, the first one is used for injection.
     *
     * @param injectors injectors requiring CpuRunner and a single operand
     * @return this
     */
    @SafeVarargs
    public final TestRunner<TCpuRunner, TOperand> injectFirst(BiConsumer<TCpuRunner, TOperand>... injectors) {
        for (BiConsumer<TCpuRunner, TOperand> injector : injectors) {
            this.injectors.add((cpuRunner, first, second) -> {
                printInjectionIfEnabled("first", first, injector);
                injector.accept(cpuRunner, first);
            });
        }
        return this;
    }

    /**
     * Inject a CpuRunner and single operand into specified injectors.
     *
     * RunnerContext has always two operands. In this case, the second one is used for injection.
     *
     * @param injectors injectors requiring CpuRunner and a single operand
     * @return this
     */
    @SafeVarargs
    public final TestRunner<TCpuRunner, TOperand> injectSecond(BiConsumer<TCpuRunner, TOperand>... injectors) {
        for (BiConsumer<TCpuRunner, TOperand> injector : injectors) {
            this.injectors.add((cpuRunner, first, second) -> {
                printInjectionIfEnabled("second", second, injector);
                injector.accept(cpuRunner, second);
            });
        }
        return this;
    }

    /**
     * Inject a CpuRunner and two operands into specified injectors.
     *
     * RunnerContext has always two operands. In this case, both are used for injection, in the order
     * (first, second).
     *
     * @param injectors injectors requiring CpuRunner and two operand
     * @return this
     */
    @SafeVarargs
    public final TestRunner<TCpuRunner, TOperand> injectTwoOperands(TwoOperInjector<TCpuRunner, TOperand>... injectors) {
        for (TwoOperInjector<TCpuRunner, TOperand> injector : injectors) {
            this.injectors.add((cpuRunner, first, second) -> {
                printInjectionIfEnabled("(first,second)", first, second, injector);
                injector.inject(cpuRunner, first, second);
            });
        }
        return this;
    }

    public void clearInjectors() {
        injectors.clear();
        injectors.addAll(injectorsToKeep);
    }

    public void clearVerifiers() {
        verifiers.clear();
        verifiers.addAll(verifiersToKeep);
    }

    public void clearAllVerifiers() {
        verifiers.clear();
        verifiersToKeep.clear();
    }

    private void printInjectionIfEnabled(String formatString, Object... arguments) {
        if (printInjectingProcess) {
            String argumentFormat = "";
            if (arguments.length > 1) {
                argumentFormat = "=%x";
            } else if (arguments.length > 2) {
                argumentFormat = "=(%x,%x)";
            }
            if (arguments.length > 0) {
                argumentFormat += " (to %s)";
            }
            System.out.println(String.format("Injecting " + formatString + argumentFormat, arguments));
        }
    }

    /**
     * Add a test verifier(s) which will be executed after the test execution.
     *
     * @param verifiers array of test verifiers
     */
    @SafeVarargs
    public final void verifyAfterTest(Consumer<RunnerContext<TOperand>>... verifiers) {
        Collections.addAll(this.verifiers, verifiers);
    }

    private void verify(RunnerContext<TOperand> context) {
        verifiers.forEach(verifier -> {
            try {
                verifier.accept(context);
            } catch (Throwable e) {
                System.err.println("Verification failed. Context: " + context + "\nVerifier: " + verifier);
                throw e;
            }
        });
    }

    /**
     * Execute the test.
     *
     * At first, it will inject CpuRunner and optionally provided operands into all injectors.
     *
     * Then, the CPU will do single step and flags saved.
     *
     * The last step is the test verification. All verifiers placed before do their job.
     *
     * @param first first operand
     * @param second second operand
     */
    @Override
    public void accept(TOperand first, TOperand second) {
        cpuRunner.reset();

        // first preserve flags; they may get overwritten by some injector
        if (flagsBefore != -1) {
            cpuRunner.setFlags(flagsBefore);
        }

        injectors.forEach(injector -> injector.inject(cpuRunner, first, second));

        RunnerContext<TOperand> context = new RunnerContext<>(
                first, second, cpuRunner.getFlags(), cpuRunner.getPC(), cpuRunner.getSP(), cpuRunner.getRegisters()
        );

        cpuRunner.step();
        flagsBefore = cpuRunner.getFlags();

        verify(context);
    }

    @Override
    public TestRunner<TCpuRunner, TOperand> clone() {
        TestRunner<TCpuRunner, TOperand> runner = new TestRunner<>(cpuRunner);
        runner.flagsBefore = flagsBefore;

        runner.injectors.addAll(this.injectors);
        runner.injectorsToKeep.addAll(this.injectorsToKeep);

        runner.verifiersToKeep.addAll(this.verifiersToKeep);
        runner.verifiers.addAll(this.verifiers);

        if (printInjectingProcess) {
            runner.printInjectingProcess();
        }

        return runner;
    }

}
