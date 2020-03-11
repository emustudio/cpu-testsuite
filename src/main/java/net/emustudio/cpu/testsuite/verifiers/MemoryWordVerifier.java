/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2020  Peter Jakubčo
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
package net.emustudio.cpu.testsuite.verifiers;

import net.emustudio.cpu.testsuite.CpuVerifier;
import net.emustudio.cpu.testsuite.RunnerContext;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class MemoryWordVerifier<T extends Number> implements Consumer<RunnerContext<T>> {
    private final Function<RunnerContext<T>, Integer> operation;
    private final Function<RunnerContext<T>, Integer> addressOperation;
    private final CpuVerifier verifier;

    public MemoryWordVerifier(CpuVerifier verifier, Function<RunnerContext<T>, Integer> operation,
                              Function<RunnerContext<T>, Integer> addressOperation) {
        this.operation = Objects.requireNonNull(operation);
        this.addressOperation = Objects.requireNonNull(addressOperation);
        this.verifier = Objects.requireNonNull(verifier);
    }

    @Override
    public void accept(RunnerContext<T> context) {
        int expectedResult = operation.apply(context);
        int address = addressOperation.apply(context);

        verifier.checkMemoryWord(address, expectedResult);
    }
}
