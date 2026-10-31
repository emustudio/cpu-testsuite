/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.verifiers;

import net.emustudio.cpu.testsuite.RunnerContext;
import net.jcip.annotations.Immutable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Checks a computed expected value at a computed memory address.
 *
 * @param <T> operand type
 */
@Immutable
public final class MemoryVerifier<T extends Number> implements Consumer<RunnerContext<T>> {
    private final BiConsumer<Integer, Integer> checkMemory;
    private final Function<RunnerContext<T>, Integer> operation;
    private final Function<RunnerContext<T>, Integer> address;

    /**
     * Creates a byte or word verifier using the corresponding CPU verifier method reference.
     *
     * @param checkMemory assertion accepting the address and expected value
     * @param operation expected-value function
     * @param address address function
     */
    public MemoryVerifier(BiConsumer<Integer, Integer> checkMemory,
                          Function<RunnerContext<T>, Integer> operation,
                          Function<RunnerContext<T>, Integer> address) {
        this.checkMemory = Objects.requireNonNull(checkMemory);
        this.operation = Objects.requireNonNull(operation);
        this.address = Objects.requireNonNull(address);
    }

    @Override
    public void accept(RunnerContext<T> context) {
        checkMemory.accept(address.apply(context), operation.apply(context));
    }
}
