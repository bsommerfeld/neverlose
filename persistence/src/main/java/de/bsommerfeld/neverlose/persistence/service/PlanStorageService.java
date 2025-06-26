package de.bsommerfeld.neverlose.persistence.service;

import de.bsommerfeld.neverlose.persistence.model.ExerciseSummary;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.model.UnitSummary;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for loading and saving TrainingPlan domain objects to a persistent storage
 * (e.g., local JSON files).
 */
public interface PlanStorageService {

  /**
   * Saves the given training plan to persistent storage. If a plan with the same identifier already
   * exists, it should be overwritten.
   *
   * @param plan The TrainingPlan domain object to save. Must not be null.
   * @return The identifier assigned to the saved plan (e.g., derived filename).
   * @throws IOException If an error occurs during saving (e.g., disk full, permissions).
   * @throws NullPointerException if plan is null.
   * @throws IllegalArgumentException if plan data is invalid for saving (optional).
   */
  String savePlan(TrainingPlan plan) throws IOException;

  /**
   * Loads a specific training plan identified by its unique identifier.
   *
   * @param uuid The unique identifier of the plan to load. Must not be null or blank.
   * @return An Optional containing the loaded TrainingPlan domain object if found, otherwise
   *     Optional.empty().
   * @throws IOException If an error occurs during loading or parsing the stored data.
   * @throws NullPointerException if planIdentifier is null.
   * @throws IllegalArgumentException if planIdentifier is blank.
   */
  Optional<TrainingPlan> loadPlan(UUID uuid) throws IOException;

  /**
   * Retrieves a list of summaries for all available training plans. Used for displaying the list of
   * plans without loading all details.
   *
   * @return A List of PlanSummary objects, sorted alphabetically by name perhaps. Returns an empty
   *     list if no plans are found.
   * @throws IOException If an error occurs reading the storage directory.
   */
  List<PlanSummary> loadPlanSummaries() throws IOException;

  /**
   * Deletes a specific training plan identified by its unique identifier.
   *
   * @param uuid The unique identifier of the plan to delete. Must not be null or blank.
   * @return true if the plan was successfully deleted, false otherwise (e.g., file not found).
   * @throws IOException If an error occurs during deletion (e.g., permissions).
   * @throws NullPointerException if planIdentifier is null.
   * @throws IllegalArgumentException if planIdentifier is blank.
   */
  boolean deletePlan(UUID uuid) throws IOException;

  /**
   * Gets the unique identifier that is used (or would be used) to store the given plan. This
   * identifier is typically used as part of the filename. The implementation should define how this
   * is generated (e.g., from name and/or internal ID).
   *
   * @param plan The TrainingPlan domain object. Must not be null.
   * @return The identifier string.
   * @throws NullPointerException if plan is null.
   */
  String getPlanIdentifier(TrainingPlan plan);

  /**
   * Saves a single training unit as a reusable template. Overwrites if a unit with the same ID
   * already exists.
   *
   * @param unit The TrainingUnit domain object to save. Must not be null.
   * @throws IOException If an error occurs during saving.
   */
  void saveUnit(TrainingUnit unit) throws IOException;

  /**
   * Loads a specific training unit identified by its unique ID.
   *
   * @param unitId The UUID of the unit to load. Must not be null.
   * @return An Optional containing the loaded TrainingUnit domain object if found, otherwise
   *     Optional.empty().
   * @throws IOException If an error occurs during loading or parsing.
   */
  Optional<TrainingUnit> loadUnit(UUID unitId) throws IOException;

  /**
   * Retrieves a list of summaries for all available training unit templates.
   *
   * @return A List of UnitSummary objects, possibly sorted by name. Empty list if none found.
   * @throws IOException If an error occurs reading the storage directory.
   */
  List<UnitSummary> loadAllUnitSummaries() throws IOException;

  /**
   * Retrieves a list of summaries for all available training unit templates.
   * This is an alias for loadAllUnitSummaries() for API consistency.
   *
   * @return A List of UnitSummary objects, possibly sorted by name. Empty list if none found.
   * @throws IOException If an error occurs reading the storage directory.
   */
  List<UnitSummary> loadUnitSummaries() throws IOException;

  /**
   * Deletes a specific training unit template identified by its unique ID.
   *
   * @param unitId The UUID of the unit to delete. Must not be null.
   * @return true if the unit was successfully deleted, false otherwise (e.g., file not found).
   * @throws IOException If an error occurs during deletion.
   */
  boolean deleteUnit(UUID unitId) throws IOException;

  /**
   * Gets the unique identifier (usually the UUID as string) used to store the given unit. Primarily
   * used internally to determine filenames.
   *
   * @param unit The TrainingUnit domain object. Must not be null.
   * @return The identifier string (typically unit.getId().toString()).
   */
  String getUnitIdentifier(TrainingUnit unit);

  /**
   * Saves a single training exercise as a reusable template. Overwrites if an exercise with the
   * same ID already exists.
   *
   * @param exercise The TrainingExercise domain object to save. Must not be null.
   * @throws IOException If an error occurs during saving.
   */
  void saveExercise(TrainingExercise exercise) throws IOException;

  /**
   * Loads a specific training exercise identified by its unique ID.
   *
   * @param exerciseId The UUID of the exercise to load. Must not be null.
   * @return An Optional containing the loaded TrainingExercise domain object if found, otherwise
   *     Optional.empty().
   * @throws IOException If an error occurs during loading or parsing.
   */
  Optional<TrainingExercise> loadExercise(UUID exerciseId) throws IOException;

  /**
   * Retrieves a list of summaries for all available training exercise templates.
   *
   * @return A List of ExerciseSummary objects, possibly sorted by name. Empty list if none found.
   * @throws IOException If an error occurs reading the storage directory.
   */
  List<ExerciseSummary> loadAllExerciseSummaries() throws IOException;

  /**
   * Retrieves a list of summaries for all available training exercise templates.
   * This is an alias for loadAllExerciseSummaries() for API consistency.
   *
   * @return A List of ExerciseSummary objects, possibly sorted by name. Empty list if none found.
   * @throws IOException If an error occurs reading the storage directory.
   */
  List<ExerciseSummary> loadExerciseSummaries() throws IOException;

  /**
   * Deletes a specific training exercise template identified by its unique ID.
   *
   * @param exerciseId The UUID of the exercise to delete. Must not be null.
   * @return true if the exercise was successfully deleted, false otherwise (e.g., file not found).
   * @throws IOException If an error occurs during deletion.
   */
  boolean deleteExercise(UUID exerciseId) throws IOException;

  /**
   * Gets the unique identifier (usually the UUID as string) used to store the given exercise.
   * Primarily used internally to determine filenames.
   *
   * @param exercise The TrainingExercise domain object. Must not be null.
   * @return The identifier string (typically exercise.getId().toString()).
   */
  String getExerciseIdentifier(TrainingExercise exercise);

  /**
   * Gets the resolved storage directory path used by this service. Useful for debugging or
   * potentially for the FileWatcher later.
   *
   * @return The Path object representing the storage directory.
   */
  Path getStoragePath();

  /**
   * Finds a training unit template by its name.
   *
   * @param name The name of the unit template to find.
   * @return An Optional containing the UUID of the unit if found, otherwise Optional.empty().
   * @throws IOException If an error occurs during the search.
   */
  Optional<UUID> findUnitIdByName(String name) throws IOException;

  /**
   * Finds a training exercise template by its name.
   *
   * @param name The name of the exercise template to find.
   * @return An Optional containing the UUID of the exercise if found, otherwise Optional.empty().
   * @throws IOException If an error occurs during the search.
   */
  Optional<UUID> findExerciseIdByName(String name) throws IOException;
}
