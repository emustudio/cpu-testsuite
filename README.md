# CPU Test Suite

![cpu-testsuite](https://github.com/emustudio/cpu-testsuite/actions/workflows/build.yml/badge.svg?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.emustudio/cpu-testsuite_11.7/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.emustudio/cpu-testsuite_11.7)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A declarative unit-testing framework for testing emuStudio CPU plug-ins. Write test specifications once, automatically generate comprehensive test cases for instruction correctness verification.

Inspired by ["QuickCheck"](https://github.com/pholser/junit-quickcheck) property-based testing.

## Features

- 🎲 **Automatic test case generation** - generates 8-bit, 16-bit, unary, and binary test cases
- 🔧 **Declarative test specification** - fluent Builder pattern API
- ⚙️ **Automatic environment setup** - manages memory, registers, and CPU flags
- 🚀 **Minimal boilerplate** - reusable test configurations
- 📊 **Comprehensive coverage** - easily test all instruction variants

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
- [Implementing Required Classes](#implementing-required-classes)
- [API Reference](#api-reference)
  - [TestBuilder Methods](#testbuilder-api-reference)
  - [Generator Methods](#generator-api-reference)
  - [Memory Operations](#memory-operations)
- [Advanced Usage](#advanced-usage-patterns)
- [Troubleshooting](#troubleshooting)
- [Examples](#examples)
- [License](#license)

## Installation

### Maven

```xml
<dependency>
  <groupId>net.emustudio</groupId>
  <artifactId>cpu-testsuite_12.1</artifactId>
  <version>1.2.0</version>
  <scope>test</scope>
</dependency>
```

### Gradle

```gradle
testImplementation 'net.emustudio:cpu-testsuite_12.1:1.2.0'
```

**Note:** Artifact name ends with major.minor emuLib version (currently `_12.1`).

## Prerequisites

The CPU Test Suite can be used if your CPU plugin meets these requirements:

- ✅ Operating memory is a collection of linearly ordered cells
- ✅ Memory cell type is `Short` or `Byte`
- ✅ CPU uses little-endian byte order
- ✅ CPU has a program counter register (or instruction pointer)
- ✅ CPU has a stack pointer register
- ✅ Instruction operands are either `Byte` (8-bit) or `Integer` (16-bit)

## Quick Start

### Overview

Testing a CPU instruction involves:

1. **Setup** - Configure initial CPU state (registers, memory, flags)
2. **Execute** - Run the instruction
3. **Verify** - Check the output (registers, flags, memory)
4. **Repeat** - Automatically for generated test cases

### Required Implementations

Each CPU requires custom implementations of:

- **`CpuRunner`** - Abstract class managing CPU execution environment
- **`CpuVerifier`** - Abstract class for verifying CPU state
- **`FlagsCheck`** - Optional abstract class for flag computation
- **`TestBuilder`** - Concrete class extending `ByteTestBuilder` or `IntegerTestBuilder`

### Simple Example

Here's a minimal example testing an 8080 SUB instruction:

```java
import static net.emustudio.cpu.testsuite.Generator.*;
import net.emustudio.cpu.testsuite.memory.ShortMemoryStub;
import net.emustudio.intel8080.impl.suite.CpuRunnerImpl;
import net.emustudio.intel8080.impl.suite.CpuVerifierImpl;
import org.junit.After;
import org.junit.Before;


public class CpuTest {
    private CpuRunnerImpl cpuRunnerImpl;
    private CpuVerifierImpl cpuVerifierImpl;
    private CpuImpl cpu;

    @Before
    public void setUp() throws PluginInitializationException {
        ShortMemoryStub memoryStub = new ShortMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);

        cpu = new CpuImpl(...);
        // simulate emuStudio boot
        cpu.initialize(...);

        cpuRunnerImpl = new CpuRunnerImpl(cpu, memoryStub);
        cpuVerifierImpl = new CpuVerifierImpl(cpu, memoryStub);

        Generator.setRandomTestsCount(10); // How many test cases should be generated
    }
    
    @After
    public void tearDown() {
        cpu.destroy();
    }
    
    @Test
    public void testSUB() {
        // ByteTestBuilder specifies that instruction operands are bytes 
        ByteTestBuilder test = new ByteTestBuilder(cpuRunnerImpl, cpuVerifierImpl)
                .firstIsRegister(REG_A)
                .verifyRegister(REG_A, context -> (context.first & 0xFF) - (context.second & 0xFF))
                .verifyFlagsOfLastOp(new FlagsBuilderImpl().sign().zero().carry().auxCarry().parity())
                .keepCurrentInjectorsAfterRun();
        
        forSome8bitBinaryWhichEqual(
                test.run(0x97)
        );
        forSome8bitBinary(
                test.secondIsRegister(REG_B).run(0x90),
                test.secondIsRegister(REG_C).run(0x91),
                test.secondIsRegister(REG_D).run(0x92),
                test.secondIsRegister(REG_E).run(0x93),
                test.secondIsRegister(REG_H).run(0x94),
                test.secondIsRegister(REG_L).run(0x95),
                test.setPair(REG_PAIR_HL, 1).secondIsMemoryByteAt(1).run(0x96)
        );
    }
}
```

It might seem complex, but all makes sense. At first, we need to know, if we operate with bytes or integers (words).
Therefore, we create new `ByteTestBuilder`. There exists also `IntegerTestBuilder` class for operating with 16-bit values.

Instruction `SUB` takes 1 argument - the register, e.g. `SUB B`, which substracts register `B` from register `A`.
In other words:

```
SUB B = A - B
```

Generally, instruction `SUB` will always be evaluated as `A - register`. Therefore we know, that first operand is always
register `A`:

```java
   .firstIsRegister(REG_A)
```

NOTE: Constant `REG_A` is defined in our 8080 CPU.

That's it for preparing the environment. Now, we want to verify, that after performing the "subtract" operation,
we get result in register `A` with the correct value:

```java
    .verifyRegister(REG_A, context -> (context.first & 0xFF) - (context.second & 0xFF))
```

We supply the computation based on the two values, which will be *generated* later. The values are accessible from
`context` object, as member values `context.first` and `context.second`. What you see above is a lambda (feature from
Java 8), taking the testing `context` object, and performing the subtract operation with given values.
 
NOTE: Here, you must be very careful; if you write the computation wrongly, the test will expect wrong results.
    
Also, the instruction is affecting flags in CPU. It is enough to specify that with the following statement:
 
```java
    .verifyFlagsOfLastOp(new FlagsBuilderImpl().sign().zero().carry().auxCarry().parity())
```
 
Here, we are saying: verify flags of the last operation (taken from the previous line - the subtract), and we supply
the flags using `FlagsBuilderImpl` class - sign, zero, carry, auxiliary carry and parity. The class however must be
implemented manually, in order to preserve the generality of the Test Suite. Each CPU has different flags with
different semantics. But don't worry, it is not difficult.

And we're almost done with the test specification. Now, we must say that after we create a test, we want to keep
the environment we set up before (in our case setting that the first operand will be stored in register `A` - before
the operation). We do this with line:
 
```java
    .keepCurrentInjectorsAfterRun();
```

And now, we can 'generate' tests for various random-generated combinations of operands. This is the strongest feature
of the suite, and frees us from creating manual examples of the instruction input and output data. It saves a lot of
time. We just say:

```java
Generator.forSome8bitBinaryWhichEqual(
        test.run(0x97)
);
```

And the generator will generate some 8-bit pair of values, which equal. And we run the test for all the generated values
on a `SUB A` instruction (which has opcode `0x97`). Here, is the trick. In this statement, we test instruction `SUB A`,
which means:

```
SUB A = A - A
```

So in order to have valid test, and we have binary values from generator (we need to have both `context.first` and
`context.second`), we need to have them *equal*, because they represent the same value - in register `A`.

The final part of the test is much more obvious:

```java
Generator.forSome8bitBinary(
        test.secondIsRegister(REG_B).run(0x90),
        test.secondIsRegister(REG_C).run(0x91),
        test.secondIsRegister(REG_D).run(0x92),
        test.secondIsRegister(REG_E).run(0x93),
        test.secondIsRegister(REG_H).run(0x94),
        test.secondIsRegister(REG_L).run(0x95),
        test.setPair(REG_PAIR_HL, 1).secondIsMemoryByteAt(1).run(0x96)
);
```

Here we want to run 7 tests, for each `SUB` variation - for registers `B`, `C`, `D`, etc. So for the specific test we
must say, that the second generated operand will be stored in the given register, before we actually 'run' the test.
Since we did not specify `keepCurrentInjectorsAfterRun()` after this step, the next step will not remember the previous
setting for the second operand. Only the first operand, for register `A` will be remembered for all tests.

The last line is interesting, with preparing register pair `HL=1` and second operand to the memory at address `1`, we
can safely run `SUB M` with opcode `0x96`, which actually does the following:

```
SUB M = A - [HL]
```

For more information, see Javadoc of the project, and real usage in available emuStudio CPU plug-ins.

---

## Core Concepts

**Test Execution Flow:** Reset → Inject State → Execute → Verify → Repeat

**Key Components:**
- **CpuRunner** - Manages CPU execution environment (extend abstract class)
- **CpuVerifier** - Verifies CPU state after execution (extend abstract class)
- **TestBuilder** - Fluent API for test specification (extend Byte/IntegerTestBuilder)
- **Generator** - Produces test cases (built-in static methods)
- **FlagsCheck** - Optional flag computation (extend abstract class)

**Operand Types:** Use `ByteTestBuilder` for 8-bit or `IntegerTestBuilder` for 16-bit operations.

---

## Implementing Required Classes

### 1. Implementing CpuRunner

```java
public class MyCpuRunner extends CpuRunner<MyCpu> {
    public MyCpuRunner(MyCpu cpu, MemoryStub<?> memoryStub) {
        super(cpu, memoryStub);
    }
    
    public int getPC() { return cpu.getProgramCounter(); }
    public int getSP() { return cpu.getStackPointer(); }
    public List<Integer> getRegisters() { /* return all register values */ }
    public void setRegister(int register, int value) { /* set by index */ }
    public void setFlags(int mask) { cpu.setFlags(mask); }
    public int getFlags() { return cpu.getFlags(); }
}
```

**Required:** Implement 6 abstract methods. **Inherited:** `setByte()`, `setProgram()`, `reset()`, `step()`

### 2. Implementing CpuVerifier

```java
public class MyCpuVerifier extends CpuVerifier {
    public MyCpuVerifier(MyCpu cpu, MemoryStub<?> memoryStub) {
        super(memoryStub);
    }
    
    public void checkFlags(int expectedFlags) { /* assert flags set */ }
    public void checkNotFlags(int expectedNotFlags) { /* assert flags not set */ }
}
```

**Required:** Implement 2 methods. **Inherited:** `checkMemoryByte()`, `checkMemoryTwoBytes()`

### 3. Implementing FlagsCheck (Optional)

```java
public class MyFlagsCheck extends FlagsCheck<Integer, MyFlagsCheck> {
    public MyFlagsCheck zero() {
        expectFlagOnlyWhen(FLAG_ZERO, (ctx, result) -> (result.intValue() & 0xFFFF) == 0);
        return this;
    }
    public MyFlagsCheck carry() {
        expectFlagOnlyWhen(FLAG_CARRY, (ctx, result) -> result.intValue() > 0xFFFF);
        return this;
    }
    // ... other flags
}
```

**Methods:** `expectFlagOnlyWhen()`, `or()`, `reset()`, `switchFirstAndSecond()`

### 4. Creating TestBuilder

```java
public class ByteTestBuilder extends TestBuilder<Byte, ByteTestBuilder, 
        MyCpuRunner, MyCpuVerifier> {
    public ByteTestBuilder(MyCpuRunner runner, MyCpuVerifier verifier) {
        super(runner, verifier);
    }
    
    // Add CPU-specific methods like firstIsRegister(), verifyRegister()
}
```

---

## API Reference

### TestBuilder API Reference

#### Environment Setup

| Method | Purpose | Example |
|--------|---------|---------|
| `.setFlags(mask)` | Set CPU flags before execution | `.setFlags(0b10101010)` |
| `.registerIsRandom(reg, max)` | Set register to random value | `.registerIsRandom(REG_C, 255)` |
| `.expandMemory(fn)` | Ensure memory size | `.expandMemory(op -> op.intValue() + 100)` |
```java
// First/second operand is byte at memory address
.firstIsMemoryByteAt(0x100)
.secondIsMemoryByteAt(0x200)

// First/second operand is word (16-bit) at memory address
.firstIsMemoryWordAt(0x100)
.secondIsMemoryWordAt(0x200)

// Use first/second operand as memory address containing a value
.firstIsMemoryAddressByte(0x42)      // Address contains byte 0x42
.secondIsMemoryAddressWord(0x1234)   // Address contains word 0x1234

// Complex: first is address, write second as word at that address
.firstIsAddressAndSecondIsMemoryWord()
.secondIsAddressAndFirstIsMemoryWord()

// Complex: first is address, write second as byte at that address
.firstIsAddressAndSecondIsMemoryByte()
.secondIsAddressAndFirstIsMemoryByte()

// Ensure memory is large enough for address calculation
.expandMemory(operand -> operand.intValue() + 100)
```

### Verification Methods

#### Register Verification (Custom)
```java
// Verify register contains expected value
.verifyRegister(REG_A, context -> context.first + context.second)
```

#### Flag Verification
```java
// Verify flags with operator
.verifyFlags(new MyFlagsCheck().sign().zero(), 
             context -> context.first - context.second)

// Verify flags of last operation (reuses last operator)
.verifyFlagsOfLastOp(new MyFlagsCheck().carry().parity())
```

#### Memory Verification
```java
// Verify byte at address
.verifyByte(0x100, context -> context.first & 0xFF)

// Verify byte at address from last operation
.verifyByte(0x100)  // Uses last operation result

// Verify byte at computed address
.verifyByte(context -> context.first.intValue(), 
            context -> context.second)

// Verify word (16-bit) at address
.verifyWord(context -> 0x100, context -> context.first + context.second)
```

#### Custom Verification
```java
// Add custom verification logic
.verifyAll(
    context -> assertEquals(expected, actual),
    context -> assertTrue(condition)
)
```

### State Management

```java
// Keep injectors after run() - reuse setup for multiple tests
.keepCurrentInjectorsAfterRun()

// Keep verifiers after run() - reuse verifications
.clearOtherVerifiersAfterRun()

// Clear all verifiers including kept ones
.clearAllVerifiers()
```

### Execution Methods

```java
// Run with no-operand instruction
.run(0x90, 0x91)  // Returns TestRunner

// Run with first operand injected into instruction
.runWithFirstOperand(0x40)

// Run with second operand injected into instruction
.runWithSecondOperand(0x41)

// Run with first operand as 8-bit value
.runWithFirst8bitOperand(0x06)

// Run with first operand as 8-bit, twice
.runWithFirst8bitOperandTwoTimes(0x40)

// Run with both operands, opcodes after operands
.runWithBothOperandsWithOpcodeAfter(0xFF, 0x40)

// Run with first 8-bit operand, opcode after
.runWithFirst8bitOperandWithOpcodeAfter(0xFF, 0x06)
```

### Debugging Methods

```java
// Enable debug output for injectors
.printInjectingProcess()

// Print operand values during test
.printOperands()

// Print register value during test
.printRegister(REG_A)
```

---

### Generator API

**Configuration:** `Generator.setRandomTestsCount(10)` - Set random test count (default: 25)

| Operand Type | Method | Test Count |
|--------------|--------|------------|
| **8-bit Binary** | `forAll8bitBinary(runners...)` | 65,536 (256×256) |
| | `forSome8bitBinary(runners...)` | Configurable |
| | `forAll8bitBinaryWhichEqual(runner)` | 256 |
| | `forSome8bitBinaryWhichEqual(runner)` | Configurable |
| **16-bit Binary** | `forAll16bitBinary(from1, from2, runner)` | Large (from start values) |
| | `forSome16bitBinary(from1, from2, runner)` | Configurable |
| | `forAll16bitBinaryWhichEqual(runner)` | 65,536 |
| | `forSome16bitBinaryWhichEqual(runner)` | Configurable |
| **8-bit Unary** | `forAll8bitUnary(runner)` | 256 |
| | `forSome8bitUnary(runner)` | Configurable |
| | `forGivenOperand(operand, runner)` | 1 |
| **16-bit Unary** | `forAll16bitUnary(from, runner)` | Large (from start) |
| | `forSome16bitUnary(from, runner)` | Configurable |
| **Gapped Range** | `forGivenOperandsAndSingleOperand(s,e1,s2,e, r)` | Range with gap |

---

## Memory Operations

**Memory Stubs:** `ByteMemoryStub` and `ShortMemoryStub` with `NumberUtils.Strategy.LITTLE_ENDIAN` or `BIG_ENDIAN`

```java
// CpuRunner - Write
cpuRunner.setByte(0x100, 0x42);
cpuRunner.setProgram(0x90, 0x91, 0x92);
cpuRunner.ensureProgramSize(1024);

// CpuVerifier - Verify
cpuVerifier.checkMemoryByte(0x100, 0x42);
cpuVerifier.checkMemoryTwoBytes(0x100, 0x1234);
```

---

## Advanced Usage Patterns

### Testing Instructions with Multiple Variants

```java
@Test
public void testADD() {
    ByteTestBuilder test = new ByteTestBuilder(cpuRunner, cpuVerifier)
        .firstIsRegister(REG_A)
        .verifyRegister(REG_A, context -> context.first + context.second)
        .verifyFlagsOfLastOp(new FlagsCheck().sign().zero().carry())
        .keepCurrentInjectorsAfterRun();
    
    // Test all register variants
    forSome8bitBinary(
        test.secondIsRegister(REG_B).run(0x80),
        test.secondIsRegister(REG_C).run(0x81),
        test.secondIsRegister(REG_D).run(0x82),
        test.secondIsRegister(REG_E).run(0x83),
        test.secondIsRegister(REG_H).run(0x84),
        test.secondIsRegister(REG_L).run(0x85)
    );
}
```

### Testing Memory-Based Instructions

```java
@Test
public void testMOV_M() {
    ByteTestBuilder test = new ByteTestBuilder(cpuRunner, cpuVerifier)
        .setPair(REG_PAIR_HL, 0x1000)
        .firstIsRegister(REG_A)
        .verifyByte(0x1000, context -> context.first);
    
    forSome8bitUnary(
        test.run(0x77)  // MOV M,A - store A at [HL]
    );
}
```

### Testing 16-bit Instructions

```java
@Test
public void testADD_HL_BC() {
    IntegerTestBuilder test = new IntegerTestBuilder(cpuRunner, cpuVerifier)
        .firstIsPair(REG_PAIR_HL)
        .secondIsPair(REG_PAIR_BC)
        .verifyPair(REG_PAIR_HL, context -> context.first + context.second)
        .verifyFlagsOfLastOp(new FlagsCheck().carry());
    
    forSome16bitBinary(0, 0,
        test.run(0x09)
    );
}
```

### Testing with Specific Values

```java
@Test
public void testRotateCarry() {
    ByteTestBuilder test = new ByteTestBuilder(cpuRunner, cpuVerifier)
        .firstIsRegister(REG_A)
        .setFlags(FLAG_CARRY)  // Set carry before operation
        .verifyRegister(REG_A, context -> {
            int value = context.first & 0xFF;
            return ((value << 1) | 1) & 0xFF;  // Rotate left with carry
        });
    
    forSome8bitUnary(
        test.run(0x17)  // RAL - rotate A left through carry
    );
}
```

### Conditional Flag Verification

```java
public class MyFlagsCheck extends FlagsCheck<Integer, MyFlagsCheck> {
    public MyFlagsCheck overflow() {
        // Overflow only on signed arithmetic
        expectFlagOnlyWhen(FLAG_OVERFLOW, (context, result) -> {
            int a = context.first.intValue();
            int b = context.second.intValue();
            int r = result.intValue();
            // Overflow if sign of result differs from expected
            boolean aPositive = (a & 0x8000) == 0;
            boolean bPositive = (b & 0x8000) == 0;
            boolean rPositive = (r & 0x8000) == 0;
            return (aPositive == bPositive) && (aPositive != rPositive);
        });
        return this;
    }
}
```

### Reusing Test Configuration

```java
@Test
public void testArithmeticInstructions() {
    ByteTestBuilder base = new ByteTestBuilder(cpuRunner, cpuVerifier)
        .firstIsRegister(REG_A)
        .secondIsRegister(REG_B)
        .keepCurrentInjectorsAfterRun();
    
    // ADD
    base.verifyRegister(REG_A, context -> context.first + context.second)
        .verifyFlagsOfLastOp(new FlagsCheck().sign().zero().carry());
    forSome8bitBinary(base.run(0x80));
    
    // SUB - clear previous verifiers, add new ones
    base.clearAllVerifiers()
        .verifyRegister(REG_A, context -> context.first - context.second)
        .verifyFlagsOfLastOp(new FlagsCheck().sign().zero().carry());
    forSome8bitBinary(base.run(0x90));
}
```

### Complex Memory Patterns

```java
@Test
public void testBlockMove() {
    ByteTestBuilder test = new ByteTestBuilder(cpuRunner, cpuVerifier)
        .firstIsMemoryByteAt(0x1000)        // Source
        .secondIsMemoryAddressByte(0x42)    // Destination address
        .verifyAll(context -> {
            int destAddr = context.second;
            int value = context.first;
            cpuVerifier.checkMemoryByte(destAddr, value);
        });
    
    forSome8bitBinary(
        test.run(0xED, 0xB0)  // LDIR or similar
    );
}
```

---

## Troubleshooting

**"Last operation is not set!"**

Provide operation in same call or set it in previous call:
```java
.verifyByte(0x100, context -> context.first + context.second)
// OR
.verifyRegister(REG_A, context -> context.first + context.second)
.verifyFlagsOfLastOp(new FlagsCheck().zero())  // Reuses lastOperation
```

**Test Fails: "PC=X expected:<breakpoint> but was:<null>"**

Ensure CPU calls `CPUListener` after `step()`:
```java
public void step() {
    executeInstruction();
    notifyStateChanged(RunState.STATE_STOPPED_BREAK);
}
```

**Wrong Flag Values**

Verify `FlagsCheck` logic:
```java
public MyFlagsCheck zero() {
    expectFlagOnlyWhen(FLAG_ZERO, (ctx, result) -> 
        (result.intValue() & 0xFF) == 0);  // Mask to proper width
    return this;
}
```

**Problem:** Flag verification fails unexpectedly.

**Solution:** Double-check your FlagsCheck logic. Use `.printInjectingProcess()` and debug output to verify:
```java
test.printInjectingProcess()
    .verifyAll(context -> {
        System.out.println("First: " + context.first);
        System.out.println("Second: " + context.second);
        System.out.println("Flags: " + Integer.toBinaryString(cpuRunner.getFlags()));
    });
```

**Tests Too Slow?**

Use `forSome*` instead of `forAll*`:
```java
Generator.setRandomTestsCount(50);
forSome8bitBinary(test.run(0x90));
```

---

## Additional Resources

- **Javadoc**: Full API documentation
- **Source Code**: [https://github.com/emustudio/emuStudio](https://github.com/emustudio/emuStudio)
- **Bug Reports**: [GitHub Issues](https://github.com/emustudio/emuStudio/issues)
- **Real Examples**: See Intel 8080 and Zilog Z80 CPU plugin tests

## License

Licensed under GPL v3.0. See LICENSE file for details.
