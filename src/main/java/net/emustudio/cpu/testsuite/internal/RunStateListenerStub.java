/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.internal;

import net.emustudio.emulib.plugins.cpu.CPU;
import net.jcip.annotations.NotThreadSafe;

/**
 * INTERNAL CLASS. DO NOT USE DIRECTLY.
 *
 */
@NotThreadSafe
public class RunStateListenerStub implements CPU.CPUListener {
    /**
     * The current run state of the CPU.
     */
    public CPU.RunState runState;

    /**
     * Creates a new run state listener stub.
     */
    public RunStateListenerStub() {
    }

    /**
     * Called when the CPU run state changes.
     *
     * @param runState the new run state
     */
    @Override
    public void runStateChanged(CPU.RunState runState) {
        this.runState = runState;
    }

    /**
     * Called when the CPU internal state changes (stub implementation - does nothing).
     */
    @Override
    public void internalStateChanged() {

    }
}
