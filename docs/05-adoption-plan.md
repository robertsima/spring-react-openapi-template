# 05 - Rolling Out Contract-First on a Modernization Program

## Metadata

| Field          | Value                                                                                    |
| -------------- | ---------------------------------------------------------------------------------------- |
| Date           | 2026-11-12                                                                               |
| Purpose        | A practical plan for introducing contract-first to teams that currently work code-first  |
| Tags           | `Modernization > Strategy`, `Process > Adoption`, `Team > Enablement`, `OpenAPI`           |
| Primary Intent | Introduce contract-first on one slice, measure it, and expand only if the numbers hold.   |

## Goal (GROW)

### Goal

Teams building replacement services and UIs agree on interfaces before they build, so integration
stops being where the schedule slips.

### Reality

Typical starting point on a modernization program:

- Several teams build against a legacy system in parallel.
- APIs are defined by whoever writes the controller first. Specs, if any, are generated afterwards.
- Integration defects (wrong field names, types, missing error cases) show up late, in shared environments.
- Some engineers see YAML-first as overhead with no visible payoff.

### Options

| Option | Effort | Risk | Notes |
| ------ | ------ | ---- | ----- |
| Mandate contract-first for all new services at once | High | High | Resistance, and mistakes get repeated across every team before anyone learns from them. |
| Pilot on one vertical slice with two teams | Low | Low | Produces your own evidence. **Recommended.** |
| Generate specs from code and add review | Low | Medium | Better than nothing, but drift is still only caught by people, not the build. |
| Do nothing | None | Ongoing | Integration rework continues at the current rate. |

### Way Forward

Run a pilot on one slice, measure it against the current baseline, then decide.

## Objectives and Key Results (pilot)

**Objective:** Show that contract-first reduces integration rework on one modernized slice.

| Key result | Baseline (measure first) | Target |
| ---------- | ------------------------ | ------ |
| Integration defects caused by interface mismatch, per release | current count | e.g. 50% lower (set your own) |
| Days the consuming team waits for provider code before starting | current average | 0 (they start from the merged contract) |
| Contract changes reviewed by a consumer before merge | n/a | 100% |
| Endpoints served that are not in the contract | current count | 0 |

## Pilot plan (SMART)

| | |
| --- | --- |
| **Specific** | One vertical slice (for example, order lookup) with one backend team and one frontend team, built from this template. |
| **Measurable** | Track the key results above from issue tracker labels and PR data. |
| **Achievable** | Uses the existing template. No new tooling beyond the generators already wired in. |
| **Relevant** | Targets integration rework, which is where modernization schedules usually slip. |
| **Time-bound** | Two to three sprints, then a go / no-go review. |

## Steps

1. **Pick the slice.** Choose one with a real consumer and a real provider, not a toy. Two teams minimum.
2. **Record the baseline.** Count recent interface-mismatch defects and waiting time on a comparable slice.
3. **Name the API owner** and agree the RACI in [03 - Day-to-day workflow](03-workflow.md).
4. **Set up from the template.** Choose the variant with [04](04-choosing-a-variant.md) and rename the
   placeholders.
5. **Write the first contract together.** Run a one-hour session where both teams write the first
   endpoints in `openapi/openapi.yaml`. This is where most of the value shows up.
6. **Build in parallel.** The frontend works against generated types and mocks. The backend implements
   the generated interfaces.
7. **Integrate, then measure.** Compare the key results to the baseline.
8. **Decide.** Expand to more slices, adjust the process, or stop. Write the result down either way.

## Prioritizing the rollout work (Eisenhower)

| | Urgent | Not urgent |
| --- | --- | --- |
| **Important** | Name an API owner per service. Pin generator versions. Block hand-written endpoints in review. | Contract tests and mock servers in CI. Shared schema library for common types (money, addresses, errors). |
| **Not important** | Perfect style rules for YAML on day one. | Publishing a developer portal from the specs. |

## Recommendations

- Start with the slice where integration hurts most. The benefit is easiest to see there.
- Keep the contract one step ahead of implementation, not months ahead.
- Treat a hand-written endpoint outside the spec as a review failure, not a style issue.
- Share the 5 Whys from [01](01-why-contract-first.md) using a real defect from your own program. It
  convinces people better than a general argument.

## Action items

- [ ] Choose the pilot slice and the two teams
- [ ] Capture the baseline numbers
- [ ] Name the API owner
- [ ] Hold the joint contract session
- [ ] Schedule the go / no-go review

## Changelog

| Date       | Description     |
| ---------- | --------------- |
| 2026-11-12 | Initial version |

## Revision History

| Version | Date       | Description     |
| ------- | ---------- | --------------- |
| 1.0     | 2026-11-12 | Initial version |
