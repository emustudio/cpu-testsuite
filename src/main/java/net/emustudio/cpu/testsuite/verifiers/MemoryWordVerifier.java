/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.verifiers;

import net.emustudio.cpu.testsuite.CpuVerifier;
import net.emustudio.cpu.testsuite.RunnerContext;
import net.jcip.annotations.Immutable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Verifier that checks if a word (16-bit value) in memory matches the expected result.
 * <p>
 * This verifier is used to validate that a CPU instruction correctly writes a word value
 * to memory. It computes both the expected value and the memory address using provided
 * functions, then verifies the actual memory contents match the expectation.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Verify that the result of adding first + second is stored at address 0x1000
 * MemoryWordVerifier<Integer> verifier = new MemoryWordVerifier<>(
 *     cpuVerifier,
 *     context -> context.first + context.second,  // Expected value
 *     context -> 0x1000                           // Memory address
 * );
 * 
 * // Use in test execution
 * RunnerContext<Integer> context = new RunnerContext<>(10, 20, 0);
 * verifier.accept(context);  // Verifies memory[0x1000] == 30
 * }</pre>
 *
 * @param <T> the type of operands (Byte or Integer)
 */
@Immutable
public class MemoryWordVerifier<T extends Number> implements Consumer<RunnerContext<T>> {
    private final Function<RunnerContext<T>, Integer> operation;
    private final Function<RunnerContext<T>, Integer> addressOperation;
    private final CpuVerifier verifier;

    /**
     * Creates a new memory word verifier.
     *
     * @param verifier the CPU verifier to use for checking memory
     * @param operation the function to compute the expected word value
     * @param addressOperation the function to compute the memory address
     */
    public MemoryWordVerifier(CpuVerifier verifier, Function<RunnerContext<T>, Integer> operation,
                              Function<RunnerContext<T>, Integer> addressOperation) {
        this.operation = Objects.requireNonNull(operation);
        this.addressOperation = Objects.requireNonNull(addressOperation);
        this.verifier = Objects.requireNonNull(verifier);
    }

    /**
     * Verifies that the memory word at the computed address matches the expected value.
     *
     * @param context the runner context containing operands and state
     */
    @Override
    public void accept(RunnerContext<T> context) {
        int expectedResult = operation.apply(context);
        int address = addressOperation.apply(context);

        verifier.checkMemoryTwoBytes(address, expectedResult);
    }
}
