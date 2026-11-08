/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CpuRunnerTest {
    @Test
    public void stepChecksReturnedTerminalState() throws Exception {
        CPU cpu = mock(CPU.class);
        when(cpu.step()).thenReturn(CPU.RunState.STATE_STOPPED_NORMAL);
        SimpleCpuRunner runner = new SimpleCpuRunner(cpu, new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN));
        runner.expectRunState(CPU.RunState.STATE_STOPPED_NORMAL);

        runner.step();
    }

    @Test
    public void stepRejectsUnexpectedReturnedState() throws Exception {
        CPU cpu = mock(CPU.class);
        when(cpu.step()).thenReturn(CPU.RunState.STATE_STOPPED_BAD_INSTR);
        SimpleCpuRunner runner = new SimpleCpuRunner(cpu, new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN));

        AssertionError failure = assertThrows(AssertionError.class, runner::step);

        assertTrue(failure.getMessage().contains("PC=0"));
        assertTrue(failure.getMessage().contains("instruction fallout"));
    }

    @Test
    public void stepPreservesExecutionFailureAsAssertionCause() throws Exception {
        CPU cpu = mock(CPU.class);
        Exception executionFailure = new Exception("Invalid instruction");
        when(cpu.step()).thenThrow(executionFailure);
        SimpleCpuRunner runner = new SimpleCpuRunner(cpu, new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN));

        AssertionError failure = assertThrows(AssertionError.class, runner::step);

        assertSame(executionFailure, failure.getCause());
    }

    @Test
    public void testEnsureProgramSizeUsesCpuAddressSpaceSize() {
        CPU cpu = mock(CPU.class);
        when(cpu.getAddressSpaceSize()).thenReturn(256);
        ByteMemoryStub memory = new ByteMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);
        SimpleCpuRunner runner = new SimpleCpuRunner(cpu, memory);

        runner.ensureProgramSize(1);

        assertEquals(256, memory.getSize());
    }
}
