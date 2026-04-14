---
theme: default
title: Reliable Performance Testing with Gatling
highlighter: shiki
lineNumbers: false
drawings:
  persist: false
transition: slide-left
mdc: true
---

# Reliable Performance Testing with Gatling

**Leon Zimmermann**

---

## The Cost of Performance Failures

In high-stakes domains — social media, ticketing, fintech — a slow system is a broken system.

<v-clicks>

- **Social media:** viral spikes bring platforms to their knees
- **Ticketing:** concert on-sale traffic causes lost sales and frustrated fans
- **Fintech:** slow transactions erode trust and trigger regulatory scrutiny

</v-clicks>

<v-click>

> Performance failures can cost millions — in revenue, reputation, and customer trust.

</v-click>

---

## The State of Performance Testing

Most teams fall into one of two traps:

<v-clicks>

- **Ad-hoc tests** — written once, never maintained, not representative of real traffic
- **Hopes and prayers** — "it worked in staging, it'll be fine"

</v-clicks>

<v-click>

Neither gives you confidence before a release.

</v-click>

---

## Enter Gatling

Gatling is a **code-first** load testing framework built for developers.

<v-clicks>

- Scenarios written in a type-safe DSL (Scala, Java, or Kotlin)
- Realistic traffic modeling with virtual users and ramp-up profiles
- Detailed HTML reports out of the box
- Designed to run in CI — no GUI required

</v-clicks>

---

## What You'll Take Away

By the end of this talk you'll have a concrete strategy to make performance testing a **reliable, repeatable** part of your delivery pipeline.

<v-clicks>

1. **CI integration** — plug Gatling into your pipeline with minimal effort
2. **Maintainable test code** — keep scenarios simple and readable
3. **Realistic user journeys** — model what real users actually do
4. **Meaningful metrics** — pick thresholds that map to business goals
5. **Common pitfalls** — know what trips teams up and how to avoid it

</v-clicks>

---

## CI Integration

*Gatling fits naturally into a build tool — no separate infrastructure needed.*

---

## Writing Maintainable Tests

*Scenarios that stay readable as your application evolves.*

---

## Modeling Real User Journeys

*Going beyond simple endpoint hammering.*

---

## Metrics and Thresholds

*Measuring what matters to the business.*

---

## Common Pitfalls

*What goes wrong — and how to sidestep it.*

---
layout: center
---

## You're Ready

Walk away with a clean, reusable test base and the confidence that your system can handle what the real world throws at it.

---
layout: end
---

# Thank You
