// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.verifiers;

import net.emustudio.cpu.testsuite.CpuVerifier;
import net.emustudio.cpu.testsuite.FlagsCheck;
import net.emustudio.cpu.testsuite.RunnerContext;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Flags verifier.
 *
 * Used as test verifier.
 *
 * @param <TOperand> operands type (Byte or Integer)
 */
public class FlagsVerifier<TOperand extends Number> implements Consumer<RunnerContext<TOperand>> {
    private final Function<RunnerContext<TOperand>, Integer> operation;
    private final FlagsCheck<TOperand, ?> flagsCheck;
    private final CpuVerifier verifier;

    /**
     * Creates new flags verifier.
     *
     * @param verifier CPU verifier
     * @param operation operation which will be used for checking flags
     * @param flagsCheck flags checker
     */
    public FlagsVerifier(CpuVerifier verifier, Function<RunnerContext<TOperand>, Integer> operation, FlagsCheck<TOperand, ?> flagsCheck) {
        this.operation = Objects.requireNonNull(operation);
        this.flagsCheck = Objects.requireNonNull(flagsCheck);
        this.verifier = Objects.requireNonNull(verifier);
    }

    @Override
    public void accept(RunnerContext<TOperand> context) {
        flagsCheck.reset();
        flagsCheck.eval(context, operation.apply(context));

        verifier.checkFlags(flagsCheck.getExpectedFlags());
        verifier.checkNotFlags(flagsCheck.getNotExpectedFlags());
    }
}
