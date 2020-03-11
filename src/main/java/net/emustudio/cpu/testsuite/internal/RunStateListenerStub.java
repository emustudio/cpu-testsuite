// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.internal;

import net.emustudio.emulib.plugins.cpu.CPU;

/**
 * INTERNAL CLASS. DO NOT USE DIRECTLY.
 *
 */
public class RunStateListenerStub implements CPU.CPUListener {
    public CPU.RunState runState;

    @Override
    public void runStateChanged(CPU.RunState runState) {
        this.runState = runState;
    }

    @Override
    public void internalStateChanged() {

    }
}
