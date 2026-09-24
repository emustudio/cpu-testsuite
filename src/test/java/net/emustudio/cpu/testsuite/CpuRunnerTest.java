/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.memory.ByteMemoryStub;
import net.emustudio.cpu.testsuite.support.SimpleCpuRunner;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CpuRunnerTest {
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
