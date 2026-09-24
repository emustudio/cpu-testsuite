# CPU Test Suite

[![CPU Test Suite Build](https://github.com/emustudio/cpu-testsuite/actions/workflows/build.yml/badge.svg)](https://github.com/emustudio/cpu-testsuite/actions/workflows/build.yml)
![Maven Central Version](https://img.shields.io/maven-central/v/net.emustudio/cpu-testsuite_12)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

CPU Test Suite is a declarative test framework for emuStudio CPU plug-ins. A test describes how operands are injected, executes one CPU instruction, and verifies registers, flags, or memory across generated operand values.

The library provides the reusable execution engine. Each CPU plug-in supplies small adapters for its own registers and flags.

## Requirements

- Java 11 or later for consumers.
- An emuStudio CPU implementing the emuLib `CPU` contract.
- A linearly addressed `Byte` or `Short` memory context.
- Little-endian instruction operands. Memory stubs can still use either emuLib word-reading strategy.
- JUnit 4 in the consumer test runtime.

The artifact suffix is the compatible emuLib major version. For emuLib 12:

### Gradle

```groovy
testImplementation 'net.emustudio:cpu-testsuite_12:1.2.0'
```

### Maven

```xml
<dependency>
  <groupId>net.emustudio</groupId>
  <artifactId>cpu-testsuite_12</artifactId>
  <version>1.2.0</version>
  <scope>test</scope>
</dependency>
```

## How a test runs

For every generated operand pair, `TestRunner` performs the same sequence:

1. Reset the CPU.
2. Restore flags retained from the previous case, if any.
3. Run all configured injectors.
4. Capture a `RunnerContext` containing operands and pre-execution CPU state.
5. Execute one CPU step.
6. Run all configured verifiers.

The main types are:

| Type | Responsibility |
| --- | --- |
| `Generator` | Produces exhaustive, random, or explicit operand values. |
| `TestBuilder` | Fluent base class that assembles injectors and verifiers. |
| `TestRunner` | Executes the reset → inject → step → verify lifecycle. |
| `CpuRunner` | CPU-specific adapter for execution, registers, flags, and memory. |
| `CpuVerifier` | CPU-specific assertion adapter. |
| `FlagsCheck` | Optional CPU-specific flag expectation builder. |
| `RunnerContext` | Immutable snapshot passed to verifiers. |

## Consumer setup

A CPU plug-in normally defines:

- a `CpuRunner` subclass implementing register, flag, PC, and SP access;
- a `CpuVerifier` subclass implementing flag assertions;
- optionally, a `FlagsCheck` subclass describing flag semantics;
- a concrete `TestBuilder` specialized for `Byte` or `Integer` operands.

`ByteTestBuilder`, `IntegerTestBuilder`, register helpers, and pair helpers are consumer conventions, not classes supplied by this library. They should extend the generic base directly, for example:

```java
final class ByteTestBuilder extends TestBuilder<
        Byte, ByteTestBuilder, MyCpuRunner, MyCpuVerifier> {

    ByteTestBuilder(MyCpuRunner runner, MyCpuVerifier verifier) {
        super(runner, verifier);
    }

    ByteTestBuilder firstIsRegister(int register) {
        runner.injectFirst((cpu, value) ->
                cpu.setRegister(register, Byte.toUnsignedInt(value)));
        return this;
    }
}
```

CPU-specific helpers can then make instruction tests concise:

```java
ByteTestBuilder test = new ByteTestBuilder(cpuRunner, cpuVerifier)
        .firstIsRegister(REG_A)
        .secondIsRegister(REG_B)       // consumer-defined helper
        .verifyRegister(               // consumer-defined helper
                REG_A,
                context -> Byte.toUnsignedInt(context.first)
                        + Byte.toUnsignedInt(context.second));

Generator.forSome8bitBinary(test.run(0x80));
```

The expectation lambda is test logic: an incorrect expression produces an incorrect expected result.

## Built-in builder operations

`TestBuilder` supplies reusable operations for:

- injecting bytes or 16-bit words at fixed memory addresses;
- using an operand as the address of a byte or word;
- writing one operand to the address represented by the other;
- setting flags or a random register value before execution;
- verifying memory bytes, words, and flags;
- constructing zero-, one-, and two-operand instruction byte sequences;
- retaining selected injectors or verifiers between generated cases.

Byte operands used as addresses are interpreted as unsigned values (`0..255`). Word values and instruction operands are written little-endian.

### Retaining configuration

`run(...)` returns a cloned `TestRunner` for that instruction and clears transient configuration from the builder.

- `keepCurrentInjectorsAfterRun()` retains injectors configured so far.
- `clearOtherVerifiersAfterRun()` retains verifiers configured so far.
- `clearAllVerifiers()` removes both transient and retained verifiers.

Call the retain method only after adding the configuration that should survive later `run(...)` calls.

## Generating operands

Random methods execute 25 cases by default. Change that process-wide value with `Generator.setRandomTestsCount(count)`. The count must be non-negative; `Generator` is not thread-safe.

| Family | Behavior |
| --- | --- |
| `forAll8bitBinary` | Runs the 32,896 unique pairs where the second unsigned value is greater than or equal to the first. |
| `forSome8bitBinary` | Runs a configurable number of random ordered byte pairs. |
| `forAll16bitBinary` | Runs the Cartesian product from the supplied starts through `0xFFFF`. |
| `forSome16bitBinary` | Runs random values uniformly within each supplied start-to-`0xFFFF` range. |
| `*BinaryWhichEqual` | Supplies the same value as both operands. |
| `*Binary* satisfying` | Restricts generated 16-bit values with predicates; random variants fail fast when a predicate has no match. |
| `*Unary` | Supplies the generated value as the first operand and zero as the second. |
| `forGivenOperandsAndSingleRun` | Executes exactly the supplied value or pair. |

Start values for 16-bit generators must be in `0..0xFFFF`. Be careful with exhaustive 16-bit binary generation: the full Cartesian product contains 4,294,967,296 cases.

## Memory and address-space behavior

`CpuRunner.ensureProgramSize(length)` grows program memory to cover both the requested length and `CPU.getAddressSpaceSize()`, preserving existing cells. This lets tests follow the CPU's declared address space instead of assuming a fixed 64 KiB memory.

The provided stubs are:

- `ByteMemoryStub` for byte cells;
- `ShortMemoryStub` for short cells.

Both implement emuLib's `MemoryContext`. Pass the required `NumberUtils.Strategy` to their constructors.

## Debugging

Useful builder methods include:

```java
test.printInjectingProcess()
    .printOperands()
    .printRegister(REG_A);
```

If `verifyFlagsOfLastOp(...)` or a memory verifier reports `Last operation is not set!`, first call an overload that accepts the expected-result function, such as `verifyByte(address, operation)` or a consumer-defined register verifier.

If `CpuRunner.step()` reports an unexpected run state, ensure the CPU notifies its `CPUListener` during the step. The default expected state is `STATE_STOPPED_BREAK`; use `expectRunState(...)` when testing a different outcome.

## Building

Gradle 9 requires JDK 17 or later to run this build; CI uses JDK 21. The compiled library still targets Java 11.

```bash
./gradlew test
./gradlew build
```

Generate the C4 architecture documentation with:

```bash
./gradlew doc
```

The entry point is `docs/output/index.html`. Diagram rendering requires Graphviz (`dot`) on `PATH`. The source is in [`docs/index.adoc`](docs/index.adoc).

## Project links

- [CPU Test Suite source](https://github.com/emustudio/cpu-testsuite)
- [emuStudio CPU plug-in implementations](https://github.com/emustudio/emuStudio/tree/master/plugins/cpu)
- [Issue tracker](https://github.com/emustudio/cpu-testsuite/issues)

## License

GNU General Public License v3.0 or later. See [`LICENSE`](LICENSE).
