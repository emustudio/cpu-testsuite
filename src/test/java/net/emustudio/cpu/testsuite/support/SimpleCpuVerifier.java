/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.support;

import net.emustudio.cpu.testsuite.CpuVerifier;
import net.emustudio.cpu.testsuite.memory.MemoryStub;

/**
 * Simple CpuVerifier implementation for testing.
 * Does nothing for flag checks.
 */
public class SimpleCpuVerifier extends CpuVerifier {
    
    public SimpleCpuVerifier(MemoryStub<?> memoryStub) {
        super(memoryStub);
    }

    @Override
    public void checkFlags(int expectedFlags) {
    }

    @Override
    public void checkNotFlags(int expectedFlags) {
    }
}
