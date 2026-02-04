/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Abstract base class for checking CPU flags after instruction execution.
 * <p>
 * This class provides a fluent API for specifying expected flag states. It allows defining
 * flags that should be set, flags that should be clear, and conditional flag expectations
 * based on predicates. The class uses a builder pattern where each method returns the
 * instance for method chaining.
 * <p>
 * Concrete implementations should extend this class and specify the specific flag constants
 * for their target CPU architecture.
 *
 * @param <T> the type of operands (Byte or Integer)
 * @param <SpecificFlagsBuilder> the concrete builder type for method chaining
 */
@SuppressWarnings({"unchecked", "unused"})
@NotThreadSafe
public abstract class FlagsCheck<T extends Number, SpecificFlagsBuilder extends FlagsCheck<T, ?>> {
    /**
     * List of evaluators that dynamically compute expected flags based on context and result.
     */
    protected final List<BiConsumer<RunnerContext<T>, Integer>> evaluators = new ArrayList<>();

    private boolean switchFirstAndSecond;
    /**
     * The flags that are expected to be set.
     */
    protected int expectedFlags = 0;
    /**
     * The flags that are expected to be clear (not set).
     */
    protected int expectedNotFlags = 0;

    /**
     * Creates a new flags checker.
     */
    protected FlagsCheck() {
    }

    /**
     * Resets the expected flags and not-expected flags to zero.
     *
     * @return this flags check instance for method chaining
     */
    public SpecificFlagsBuilder reset() {
        expectedFlags = 0;
        expectedNotFlags = 0;
        return (SpecificFlagsBuilder) this;
    }

    /**
     * Adds the specified flags to the expected flags using bitwise OR.
     *
     * @param flags the flags to add to expected flags
     * @return this flags check instance for method chaining
     */
    public SpecificFlagsBuilder or(int flags) {
        expectedFlags |= flags;
        return (SpecificFlagsBuilder) this;
    }

    /**
     * Toggles whether the first and second operands should be switched during evaluation.
     *
     * @return this flags check instance for method chaining
     */
    public SpecificFlagsBuilder switchFirstAndSecond() {
        switchFirstAndSecond = !switchFirstAndSecond;
        return (SpecificFlagsBuilder) this;
    }

    /**
     * Adds a conditional flag expectation based on a predicate.
     * If the predicate returns true, the flag is expected to be set; otherwise, it's expected to be clear.
     *
     * @param flag the flag to check
     * @param predicate the condition determining whether the flag should be set
     * @return this flags check instance for method chaining
     */
    public SpecificFlagsBuilder expectFlagOnlyWhen(int flag, BiFunction<RunnerContext<T>, Number, Boolean> predicate) {
        evaluators.add(((context, result) -> {
            if (predicate.apply(context, result)) {
                expectedFlags |= flag;
            } else {
                expectedNotFlags |= flag;
            }
        }));
        return (SpecificFlagsBuilder) this;
    }

    /**
     * Returns the flags that are expected to be set.
     *
     * @return the expected flags mask
     */
    public int getExpectedFlags() {
        return expectedFlags;
    }

    /**
     * Returns the flags that are expected to be clear (not set).
     *
     * @return the not-expected flags mask
     */
    public int getNotExpectedFlags() {
        return expectedNotFlags;
    }

    /**
     * Evaluates all registered evaluators with the given context and result.
     * This updates the expected and not-expected flags based on the evaluators' logic.
     *
     * @param context the runner context containing operands and state
     * @param result the operation result
     */
    public void eval(RunnerContext<T> context, int result) {
        for (BiConsumer<RunnerContext<T>, Integer> evaluator : evaluators) {
            if (switchFirstAndSecond) {
                evaluator.accept(context.switchFirstAndSecond(), result);
            } else {
                evaluator.accept(context, result);
            }
        }
    }

}
