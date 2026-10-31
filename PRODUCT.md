# Product

<!-- impeccable:product-schema 1 -->

## Platform

Java library used inside a JUnit test JVM. This repository has no application UI.

## Users

emuStudio CPU plugin developers who write and maintain instruction correctness tests.
They supply CPU-specific adapters and describe expected register, flag, and memory
behavior through a fluent Java API.

## Product Purpose

CPU Test Suite makes instruction tests concise and reusable. Developers describe
state injection and expected outcomes once, then exercise the real CPU plugin
across generated operand values. Success means detecting instruction behavior
mismatches with less repeated test setup.

## Operating Context

Consumers add the library as a test dependency and run their tests through JUnit 4
with Gradle or Maven. Each CPU plugin provides its own runner, verifier, and test
builder adapters, with optional flag expectation helpers.

For each generated operand pair, the framework resets the CPU, restores retained
flags, injects state, captures the pre-execution context, executes one instruction,
and verifies the result.

## Capabilities and Constraints

- Keep the shared framework CPU-independent; CPU-specific behavior belongs in
  consumer adapters.
- Preserve Java 11 compatibility for consumers.
- Integrate with JUnit 4 and the emuLib CPU and memory contracts.
- Supply exhaustive, random, and explicit operand generation, reusable state
  injectors, result verifiers, and byte- or short-cell memory stubs.
- Use the configured memory word strategy consistently for word injection,
  instruction operands, and verification.
- Expected-result functions remain consumer test logic; the framework executes
  those expectations rather than defining instruction semantics.

## Evidence on Hand

- `README.md`: consumer setup, API examples, execution lifecycle, and build commands.
- `docs/index.adoc` and `docs/c4/`: architecture and responsibility boundaries.
- `src/test/java/`: tests of the framework's behavior.
- `build.gradle`: dependency, publication, and Java compatibility configuration.

## Product Principles

- Share instruction-test mechanics while keeping CPU semantics in consumer code.
- Make state setup, operand generation, and expected outcomes explicit.
- Keep injection and verification consistent with the configured memory model.
- Preserve the confirmed consumer runtime and integration contracts.
