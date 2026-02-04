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
 * Verifier that checks if a byte value in memory matches the expected result.
 * <p>
 * This verifier is used to validate that a CPU instruction correctly writes a byte value
 * to memory. It computes both the expected value and the memory address using provided
 * functions, then verifies the actual memory contents match the expectation.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Verify that the result of adding first + second is stored at address 0x2000
 * MemoryByteVerifier<Byte> verifier = new MemoryByteVerifier<>(
 *     cpuVerifier,
 *     context -> (context.first + context.second) & 0xFF,  // Expected value
 *     context -> 0x2000                                     // Memory address
 * );
 * 
 * // Use in test execution
 * RunnerContext<Byte> context = new RunnerContext<>((byte)10, (byte)20, 0);
 * verifier.accept(context);  // Verifies memory[0x2000] == 30
 * }</pre>
 *
 * @param <T> the type of operands (Byte or Integer)
 */
@Immutable
public class MemoryByteVerifier<T extends Number> implements Consumer<RunnerContext<T>> {
    private final Function<RunnerContext<T>, Integer> operation;
    private final CpuVerifier verifier;
    private final Function<RunnerContext<T>, Integer> address;

    /**
     * Creates a new memory byte verifier.
     *
     * @param verifier the CPU verifier to use for checking memory
     * @param operation the function to compute the expected byte value
     * @param addressOperator the function to compute the memory address
     */
    public MemoryByteVerifier(CpuVerifier verifier, Function<RunnerContext<T>, Integer> operation,
                              Function<RunnerContext<T>, Integer> addressOperator) {
        this.operation = Objects.requireNonNull(operation);
        this.verifier = Objects.requireNonNull(verifier);
        this.address = Objects.requireNonNull(addressOperator);
    }

    /**
     * Verifies that the memory byte at the computed address matches the expected value.
     *
     * @param context the runner context containing operands and state
     */
    @Override
    public void accept(RunnerContext<T> context) {
        verifier.checkMemoryByte(address.apply(context), operation.apply(context));
    }
}
