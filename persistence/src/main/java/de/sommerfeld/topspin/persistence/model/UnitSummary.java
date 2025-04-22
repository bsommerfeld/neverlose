package de.sommerfeld.topspin.persistence.model;

import java.util.UUID;

/**
 * Data Transfer Object holding summary information for a saved TrainingUnit template.
 *
 * @param identifier Unique identifier (UUID) of the unit.
 * @param name       The user-defined name of the unit.
 */
public record UnitSummary(UUID identifier, String name) {
}