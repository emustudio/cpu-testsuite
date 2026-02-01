/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;


import net.emustudio.emulib.plugins.memory.MemoryContext;

@SuppressWarnings("unused")
public interface MemoryStub<T extends Number> extends MemoryContext<T> {

    void setMemory(short[] memory);

    int getWordReadingStrategy();
}
