/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuVerifier;
import net.emustudio.cpu.testsuite.verifiers.FlagsVerifier;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class FlagsCheckTest {
    private static class Check extends FlagsCheck<Integer, Check> {
    }

    @Test
    public void unconditionalExpectationIsCheckedOnEveryVerification() {
        Check check = new Check().or(1);
        int[] actualFlags = {0};
        SimpleCpuVerifier verifier = new SimpleCpuVerifier(new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN)) {
            @Override
            public void checkFlags(int mask) {
                assertEquals(mask, actualFlags[0] & mask);
            }
        };
        FlagsVerifier<Integer> flags = new FlagsVerifier<>(verifier, context -> 0, check);

        assertThrows(AssertionError.class, () -> flags.accept(new RunnerContext<>(0, 0, 0)));
        assertThrows(AssertionError.class, () -> flags.accept(new RunnerContext<>(1, 0, 0)));
        assertEquals(1, check.getExpectedFlags());
        actualFlags[0] = 1;
        flags.accept(new RunnerContext<>(2, 0, 0));
    }

    @Test
    public void conditionalMasksAreRecomputedWithoutLosingUnconditionalFlags() {
        Check check = new Check().or(1).expectFlagOnlyWhen(2, (context, result) -> result.intValue() == 0);

        check.eval(new RunnerContext<>(0, 0, 0), 0);
        assertEquals(3, check.getExpectedFlags());
        assertEquals(0, check.getNotExpectedFlags());

        check.eval(new RunnerContext<>(1, 0, 0), 1);
        assertEquals(1, check.getExpectedFlags());
        assertEquals(2, check.getNotExpectedFlags());

        check.reset();
        assertEquals(1, check.getExpectedFlags());
        assertEquals(0, check.getNotExpectedFlags());
    }

    @Test
    public void switchedOperandsApplyToEveryRuleWithoutChangingOriginalContext() {
        RunnerContext<Integer> context = new RunnerContext<>(1, 2, 0);
        Check check = new Check().switchFirstAndSecond()
                .expectFlagOnlyWhen(1, (c, result) -> c.first == 2)
                .expectFlagOnlyWhen(2, (c, result) -> c.second == 1);

        check.eval(context, 0);
        assertEquals(3, check.getExpectedFlags());
        assertEquals(Integer.valueOf(1), context.first);
        assertEquals(Integer.valueOf(2), context.second);
    }
}
