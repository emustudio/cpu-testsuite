/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.support;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.memory.MemoryStub;
import net.emustudio.emulib.plugins.cpu.CPU;

import java.util.Collections;
import java.util.List;

/**
 * Simple CpuRunner implementation for testing.
 * Returns empty/zero values for all methods.
 */
public class SimpleCpuRunner extends CpuRunner<CPU> {
    
    public SimpleCpuRunner(CPU cpu, MemoryStub<?> memoryStub) {
        super(cpu, memoryStub);
    }

    @Override
    public int getPC() {
        return 0;
    }

    @Override
    public int getSP() {
        return 0;
    }

    @Override
    public List<Integer> getRegisters() {
        return Collections.emptyList();
    }

    @Override
    public void setRegister(int register, int value) {
    }

    @Override
    public void setFlags(int mask) {
    }

    @Override
    public int getFlags() {
        return 0;
    }
}
