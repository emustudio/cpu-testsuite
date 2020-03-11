// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@SuppressWarnings({"unchecked", "unused"})
public abstract class FlagsCheck<T extends Number, SpecificFlagsBuilder extends FlagsCheck<T, ?>> {
    protected final List<BiConsumer<RunnerContext<T>, Integer>> evaluators = new ArrayList<>();

    private boolean switchFirstAndSecond;
    protected int expectedFlags = 0;
    protected int expectedNotFlags = 0;

    public SpecificFlagsBuilder reset() {
        expectedFlags = 0;
        expectedNotFlags = 0;
        return (SpecificFlagsBuilder)this;
    }

    public SpecificFlagsBuilder or(int flags) {
        expectedFlags |= flags;
        return (SpecificFlagsBuilder)this;
    }

    public SpecificFlagsBuilder switchFirstAndSecond() {
        switchFirstAndSecond = !switchFirstAndSecond;
        return (SpecificFlagsBuilder)this;
    }

    public SpecificFlagsBuilder expectFlagOnlyWhen(int flag, BiFunction<RunnerContext<T>, Number, Boolean> predicate) {
        evaluators.add(((context, result) -> {
            if (predicate.apply(context, result)) {
                expectedFlags |= flag;
            } else {
                expectedNotFlags |= flag;
            }
        }));
        return (SpecificFlagsBuilder)this;
    }

    public int getExpectedFlags() {
        return expectedFlags;
    }

    public int getNotExpectedFlags() {
        return expectedNotFlags;
    }

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
