---
name: Non-functional requirement
about: Report a violated or missing quality attribute (performance or reliability) — not a functional bug or feature request
title: "[NFR]"
labels: non-functional requirement
assignees: aslopek

---

IMPORTANT: This template is for **non-functional requirements only** — the system does what it's
supposed to do, but *how well* it does it (speed, resource usage, availability, security posture,
etc.) is unacceptable. If the system produces a wrong result, use the Bug report template instead.
If it's missing a capability entirely, use the Feature request template instead.

**Category**

One of: `Performance` | `Reliability`

**Describe the requirement**

A clear and concise description of the quality attribute that is deficient, and why it matters.

**Measurement method**

How the acceptance criteria below can be verified objectively — tool, script, profiler, load-test
scenario, dataset/environment used. If there's no repeatable way to measure this yet, say so
explicitly — that's itself a gap worth calling out before any threshold can be trusted.

**Acceptance criteria**

NFRs don't fit GIVEN/WHEN/THEN well — there's no single state transition, just a metric that must
clear a bar under stated conditions. Use "Planguage"-style criteria instead, one block per metric.

**AC1: <Tag — short name for the metric>**
**Scale:** <The unit being measured and exactly how it's defined, e.g. "wall-clock time from
request to response for GET /x, measured server-side">
**Meter:** <The concrete measurement procedure — test name, script, tool, dataset/environment>
**Past:** <Current/baseline value, if known — write "not yet measured" if it isn't>
**Must:** <The fail threshold — below this, the requirement is not met>
**Plan:** <The target threshold — what a successful fix should achieve>
**Wish:** <Optional stretch goal, if there's headroom worth aiming for beyond Plan>

**Suspected cause (optional)**

If already investigated, the suspected root cause with file/line references. Leave blank if this
issue is reporting a symptom only and the cause still needs investigating.

**Additional context**

Add any other context, profiling output, or related issues here.
