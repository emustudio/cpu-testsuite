// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;

/**
 * Injector used with two operands (standard Java does not have 3-argument consumer).
 *
 * @param <TCpuRunner> CpuRunner type
 * @param <TOperand> type of operands (Byte or Integer). Both are of the same type.
 */
@FunctionalInterface
public interface TwoOperInjector<TCpuRunner extends CpuRunner<?>, TOperand extends Number> {
    void inject(TCpuRunner cpuRunner, TOperand first, TOperand second);
}
