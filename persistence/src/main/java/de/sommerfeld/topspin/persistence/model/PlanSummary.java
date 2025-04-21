package de.sommerfeld.topspin.persistence.model;

/**
 * Data Transfer Object holding summary information for a saved training plan,
 * typically used for listing available plans.
 *
 * @param identifier Unique identifier for the plan (e.g., filename without extension or UUID string).
 * @param name       The user-defined name of the plan.
 */
public record PlanSummary(String identifier, String name) {
}