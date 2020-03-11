// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.injectors.internal.Utils;
import net.jcip.annotations.Immutable;

import java.util.Collections;
import java.util.List;

/**
 * Context of a running test.
 *
 * It is used by injectors and verifiers.
 *
 * @param <TOperand> type of the operands (Byte or Integer)
 */
@SuppressWarnings("unused")
@Immutable
public class RunnerContext<TOperand extends Number> {
    public final TOperand first;
    public final TOperand second;

    public final int flags;
    public final int PC;
    public final int SP;
    public final List<Integer> registers;

    /**
     * Creates new RunnerContext which will be used by test verifiers.
     *
     * @param first first operand (if not used, 0)
     * @param second second operand (if not used, 0)
     * @param flags flags before test execution
     * @param PC program context register (or instruction pointer) before test execution
     * @param SP stack pointer before test execution
     * @param registers values of some CPU registers before test execution (which registers are there is up to
     *                  CpuRunner implementation)
     */
    public RunnerContext(TOperand first, TOperand second, int flags, int PC, int SP, List<Integer> registers) {
        this.first = first;
        this.second = second;
        this.flags = flags;
        this.PC = PC;
        this.SP = SP;

        this.registers = List.copyOf(registers);
    }

    /**
     * Creates new RunnerContext which will be used by test verifiers.
     *
     * NOTE: PC, SP will be 0, and registers will be empty
     *
     * @param first first operand (if not used, 0)
     * @param second second operand (if not used, 0)
     * @param flags flags before test execution
     */
    public RunnerContext(TOperand first, TOperand second, int flags) {
        this(first, second, flags, 0, 0, Collections.emptyList());
    }

    /**
     * Creates new running context which will preserve everything but the first and second operands will be switched
     * (first becomes second and vice versa).
     *
     * @return new runner context with switched first and second operand
     */
    public RunnerContext<TOperand> switchFirstAndSecond() {
        return new RunnerContext<>(second, first, flags, PC, SP, registers);
    }

    /**
     * Get a register value
     *
     * @param register index of the register
     * @return register value before test execution
     */
    public int getRegister(int register) {
        return registers.get(register);
    }

    @Override
    public String toString() {
        return "RunnerContext{" +
            "operands=" + Utils.toHexString(first, second) +
            ", flags=" + Integer.toHexString(flags) +
            ", PC=" + Integer.toHexString(PC) +
            ", SP=" + Integer.toHexString(SP) +
            ", registers=" + Utils.toHexString(registers.toArray()) +
            '}';
    }
}
