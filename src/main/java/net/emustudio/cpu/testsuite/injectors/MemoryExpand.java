/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;

import java.util.function.BiConsumer;

/**
 * Program memory expander.
 *
 * Used as an injector for TestRunner.
 *
 * Ensures that memory has at least specified size. The size is injected from TestRunner.
 *
 */
@SuppressWarnings("unused")
public class MemoryExpand<TCpuRunner extends CpuRunner<?>> implements BiConsumer<TCpuRunner, Integer> {

    @Override
    public void accept(TCpuRunner cpuRunner, Integer address) {
        cpuRunner.ensureProgramSize(address + 4);
    }

    @Override
    public String toString() {
        return "memoryExpander";
    }
}
