/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.verifiers;

import net.emustudio.cpu.testsuite.RunnerContext;
import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuVerifier;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class MemoryVerifierTest {
    @Test
    public void byteAndWordChecksUseTheComputedAddressAndExpectedValue() {
        ByteMemoryStub memory = new ByteMemoryStub(NumberUtils.Strategy.BIG_ENDIAN);
        memory.setMemory(new byte[] {0, 0, 0, 0x12, 0x34});
        SimpleCpuVerifier cpuVerifier = new SimpleCpuVerifier(memory);
        RunnerContext<Integer> context = new RunnerContext<>(3, 0x1234, 0);
        MemoryVerifier<Integer> word = new MemoryVerifier<>(cpuVerifier::checkMemoryTwoBytes, c -> c.second, c -> c.first);
        MemoryVerifier<Integer> singleByte = new MemoryVerifier<>(cpuVerifier::checkMemoryByte, c -> c.second >>> 8, c -> c.first);

        word.accept(context);
        singleByte.accept(context);
        memory.write(3, (byte) 0);
        assertThrows(AssertionError.class, () -> word.accept(context));
        assertThrows(AssertionError.class, () -> singleByte.accept(context));
    }

    @Test
    public void missingCheckAddressOrExpectationIsRejectedDuringSetup() {
        assertThrows(NullPointerException.class, () -> new MemoryVerifier<Integer>(null, c -> 1, c -> 0));
        assertThrows(NullPointerException.class, () -> new MemoryVerifier<Integer>((address, expected) -> {}, null, c -> 0));
        assertThrows(NullPointerException.class, () -> new MemoryVerifier<Integer>((address, expected) -> {}, c -> 1, null));
    }
}
