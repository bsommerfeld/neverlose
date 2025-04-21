package de.sommerfeld.topspin.persistence.service;

import de.sommerfeld.topspin.persistence.model.PlanSummary;
import de.sommerfeld.topspin.plan.TrainingPlan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for loading and saving TrainingPlan domain objects
 * to a persistent storage (e.g., local JSON files).
 */
public interface PlanStorageService {

    /**
     * Saves the given training plan to persistent storage.
     * If a plan with the same identifier already exists, it should be overwritten.
     *
     * @param plan The TrainingPlan domain object to save. Must not be null.
     * @return The identifier assigned to the saved plan (e.g., derived filename).
     * @throws IOException              If an error occurs during saving (e.g., disk full, permissions).
     * @throws NullPointerException     if plan is null.
     * @throws IllegalArgumentException if plan data is invalid for saving (optional).
     */
    String savePlan(TrainingPlan plan) throws IOException;

    /**
     * Loads a specific training plan identified by its unique identifier.
     *
     * @param planIdentifier The unique identifier (e.g., filename without extension)
     *                       of the plan to load. Must not be null or blank.
     * @return An Optional containing the loaded TrainingPlan domain object if found,
     * otherwise Optional.empty().
     * @throws IOException              If an error occurs during loading or parsing the stored data.
     * @throws NullPointerException     if planIdentifier is null.
     * @throws IllegalArgumentException if planIdentifier is blank.
     */
    Optional<TrainingPlan> loadPlan(String planIdentifier) throws IOException;

    /**
     * Retrieves a list of summaries for all available training plans.
     * Used for displaying the list of plans without loading all details.
     *
     * @return A List of PlanSummary objects, sorted alphabetically by name perhaps.
     * Returns an empty list if no plans are found.
     * @throws IOException If an error occurs reading the storage directory.
     */
    List<PlanSummary> loadPlanSummaries() throws IOException;

    /**
     * Deletes a specific training plan identified by its unique identifier.
     *
     * @param planIdentifier The unique identifier of the plan to delete. Must not be null or blank.
     * @return true if the plan was successfully deleted, false otherwise (e.g., file not found).
     * @throws IOException              If an error occurs during deletion (e.g., permissions).
     * @throws NullPointerException     if planIdentifier is null.
     * @throws IllegalArgumentException if planIdentifier is blank.
     */
    boolean deletePlan(String planIdentifier) throws IOException;

    /**
     * Gets the unique identifier that is used (or would be used) to store the given plan.
     * This identifier is typically used as part of the filename. The implementation
     * should define how this is generated (e.g., from name and/or internal ID).
     *
     * @param plan The TrainingPlan domain object. Must not be null.
     * @return The identifier string.
     * @throws NullPointerException if plan is null.
     */
    String getPlanIdentifier(TrainingPlan plan);

    /**
     * Gets the resolved storage directory path used by this service.
     * Useful for debugging or potentially for the FileWatcher later.
     *
     * @return The Path object representing the storage directory.
     */
    Path getStoragePath();

}
