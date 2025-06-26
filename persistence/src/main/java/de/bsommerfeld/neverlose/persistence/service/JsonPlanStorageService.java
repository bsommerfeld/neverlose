package de.bsommerfeld.neverlose.persistence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.dto.TrainingExerciseDTO;
import de.bsommerfeld.neverlose.persistence.dto.TrainingPlanDTO;
import de.bsommerfeld.neverlose.persistence.dto.TrainingUnitDTO;
import de.bsommerfeld.neverlose.persistence.mapper.PlanMapper;
import de.bsommerfeld.neverlose.persistence.model.ExerciseSummary;
import de.bsommerfeld.neverlose.persistence.model.PlanSummary;
import de.bsommerfeld.neverlose.persistence.model.UnitSummary;
import de.bsommerfeld.neverlose.plan.TrainingPlan;
import de.bsommerfeld.neverlose.plan.components.TrainingExercise;
import de.bsommerfeld.neverlose.plan.components.TrainingUnit;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

/**
 * Service implementation for loading and saving TrainingPlan domain objects as JSON files in a
 * designated storage directory.
 */
public class JsonPlanStorageService implements PlanStorageService {

  private static final LogFacade log = LogFacadeFactory.getLogger();
  private static final String JSON_FILE_EXTENSION = ".json";
  private static final String PLANS_DIR = "plans";
  private static final String UNITS_DIR = "units";
  private static final String EXERCISES_DIR = "exercises";

  private final ObjectMapper objectMapper;
  private final PlanMapper planMapper;
  private final Path storageDirectory;
  private final Path plansPath;
  private final Path unitsPath;
  private final Path exercisesPath;

  @Inject
  public JsonPlanStorageService(
      ObjectMapper objectMapper,
      PlanMapper planMapper,
      @Named("storage.directory.path") Path storageDirectory) {
    this.objectMapper = Objects.requireNonNull(objectMapper);
    this.planMapper = Objects.requireNonNull(planMapper);
    this.storageDirectory = Objects.requireNonNull(storageDirectory);

    this.plansPath = storageDirectory.resolve(PLANS_DIR);
    this.unitsPath = storageDirectory.resolve(UNITS_DIR);
    this.exercisesPath = storageDirectory.resolve(EXERCISES_DIR);

    ensureStorageDirectoryExists(this.storageDirectory);
    ensureStorageDirectoryExists(this.plansPath);
    ensureStorageDirectoryExists(this.unitsPath);
    ensureStorageDirectoryExists(this.exercisesPath);

    log.info("Initialized JsonPlanStorageService. Storage Directory: {}", this.storageDirectory);
  }

  private Path getPlanFilePath(UUID planId) {
    return plansPath.resolve(planId.toString() + JSON_FILE_EXTENSION);
  }

  private Path getUnitFilePath(UUID unitId) {
    return unitsPath.resolve(unitId.toString() + JSON_FILE_EXTENSION);
  }

  private Path getExerciseFilePath(UUID exerciseId) {
    return exercisesPath.resolve(exerciseId.toString() + JSON_FILE_EXTENSION);
  }

  @Override
  public String getPlanIdentifier(TrainingPlan plan) {
    Objects.requireNonNull(plan, "TrainingPlan cannot be null");
    return plan.getId().toString();
  }

  @Override
  public String getUnitIdentifier(TrainingUnit unit) {
    Objects.requireNonNull(unit, "TrainingUnit cannot be null");
    return unit.getId().toString();
  }

  @Override
  public String getExerciseIdentifier(TrainingExercise exercise) {
    Objects.requireNonNull(exercise, "TrainingExercise cannot be null");
    return exercise.getId().toString();
  }

  @Override
  public String savePlan(TrainingPlan plan) throws IOException {
    Objects.requireNonNull(plan, "TrainingPlan cannot be null");
    Path filePath = getPlanFilePath(plan.getId());
    log.debug(
        "Saving plan '{}' with identifier {} to file: {}", plan.getName(), plan.getId(), filePath);
    TrainingPlanDTO dto = planMapper.toDTO(plan);
    saveDtoToFile(dto, filePath);
    log.info("Successfully saved plan '{}' to {}", plan.getName(), filePath.getFileName());
    return plan.getId().toString();
  }

  @Override
  public Optional<TrainingPlan> loadPlan(UUID planId) throws IOException {
    Objects.requireNonNull(planId, "planId cannot be null");
    Path filePath = getPlanFilePath(planId);
    log.debug("Attempting to load plan from: {}", filePath);

    if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
      log.warn("Plan file not found or not readable: {}", filePath);
      return Optional.empty();
    }

    try (InputStream in = Files.newInputStream(filePath)) {
      TrainingPlanDTO dto = objectMapper.readValue(in, TrainingPlanDTO.class);
      TrainingPlan plan = planMapper.toDomain(dto);
      log.info("Successfully loaded plan '{}' from {}", plan.getName(), filePath.getFileName());
      return Optional.of(plan);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse JSON for plan file: {}", filePath, e);
      throw new IOException("Failed to parse plan file: " + filePath.getFileName(), e);
    } catch (Exception e) { // Catch potential mapping errors
      log.error("Failed to map DTO to domain object for plan file: {}", filePath, e);
      throw new IOException("Failed to process plan file: " + filePath.getFileName(), e);
    }
  }

  @Override
  public List<PlanSummary> loadPlanSummaries() throws IOException {
    log.debug("Loading plan summaries from directory: {}", plansPath);
    return loadSummaries(plansPath, PlanSummary.class, "Plan");
  }

  @Override
  public boolean deletePlan(UUID planId) throws IOException {
    Objects.requireNonNull(planId, "planId cannot be null");
    Path filePath = getPlanFilePath(planId);
    return deleteFile(filePath, "Plan");
  }

  @Override
  public void saveUnit(TrainingUnit unit) throws IOException {
    Objects.requireNonNull(unit, "TrainingUnit cannot be null");
    Path filePath = getUnitFilePath(unit.getId());
    log.debug(
        "Saving unit '{}' with identifier {} to file: {}", unit.getName(), unit.getId(), filePath);
    TrainingUnitDTO dto = planMapper.toDTO(unit);
    saveDtoToFile(dto, filePath);
    log.info("Successfully saved unit '{}' to {}", unit.getName(), filePath.getFileName());
  }

  @Override
  public Optional<TrainingUnit> loadUnit(UUID unitId) throws IOException {
    Objects.requireNonNull(unitId, "unitId cannot be null");
    Path filePath = getUnitFilePath(unitId);
    log.debug("Attempting to load unit from: {}", filePath);

    if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
      log.warn("Unit file not found or not readable: {}", filePath);
      return Optional.empty();
    }

    try (InputStream in = Files.newInputStream(filePath)) {
      TrainingUnitDTO dto = objectMapper.readValue(in, TrainingUnitDTO.class);
      TrainingUnit unit = planMapper.toDomain(dto);
      log.info("Successfully loaded unit '{}' from {}", unit.getName(), filePath.getFileName());
      return Optional.of(unit);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse JSON for unit file: {}", filePath, e);
      throw new IOException("Failed to parse unit file: " + filePath.getFileName(), e);
    } catch (Exception e) {
      log.error("Failed to map DTO to domain object for unit file: {}", filePath, e);
      throw new IOException("Failed to process unit file: " + filePath.getFileName(), e);
    }
  }

  @Override
  public List<UnitSummary> loadAllUnitSummaries() throws IOException {
    log.debug("Loading unit summaries from directory: {}", unitsPath);
    return loadSummaries(unitsPath, UnitSummary.class, "Unit");
  }

  @Override
  public List<UnitSummary> loadUnitSummaries() throws IOException {
    // This is an alias for loadAllUnitSummaries() for API consistency
    return loadAllUnitSummaries();
  }

  @Override
  public boolean deleteUnit(UUID unitId) throws IOException {
    Objects.requireNonNull(unitId, "unitId cannot be null");
    Path filePath = getUnitFilePath(unitId);
    return deleteFile(filePath, "Unit");
  }

  @Override
  public void saveExercise(TrainingExercise exercise) throws IOException {
    Objects.requireNonNull(exercise, "TrainingExercise cannot be null");
    Path filePath = getExerciseFilePath(exercise.getId());
    log.debug(
        "Saving exercise '{}' with identifier {} to file: {}",
        exercise.getName(),
        exercise.getId(),
        filePath);
    TrainingExerciseDTO dto = planMapper.toDTO(exercise);
    saveDtoToFile(dto, filePath);
    log.info("Successfully saved exercise '{}' to {}", exercise.getName(), filePath.getFileName());
  }

  @Override
  public Optional<TrainingExercise> loadExercise(UUID exerciseId) throws IOException {
    Objects.requireNonNull(exerciseId, "exerciseId cannot be null");
    Path filePath = getExerciseFilePath(exerciseId);
    log.debug("Attempting to load exercise from: {}", filePath);

    if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
      log.warn("Exercise file not found or not readable: {}", filePath);
      return Optional.empty();
    }

    try (InputStream in = Files.newInputStream(filePath)) {
      TrainingExerciseDTO dto = objectMapper.readValue(in, TrainingExerciseDTO.class);
      TrainingExercise exercise = planMapper.toDomain(dto);
      log.info(
          "Successfully loaded exercise '{}' from {}", exercise.getName(), filePath.getFileName());
      return Optional.of(exercise);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse JSON for exercise file: {}", filePath, e);
      throw new IOException("Failed to parse exercise file: " + filePath.getFileName(), e);
    } catch (Exception e) {
      log.error("Failed to map DTO to domain object for exercise file: {}", filePath, e);
      throw new IOException("Failed to process exercise file: " + filePath.getFileName(), e);
    }
  }

  @Override
  public List<ExerciseSummary> loadAllExerciseSummaries() throws IOException {
    log.debug("Loading exercise summaries from directory: {}", exercisesPath);
    return loadSummaries(exercisesPath, ExerciseSummary.class, "Exercise");
  }

  @Override
  public List<ExerciseSummary> loadExerciseSummaries() throws IOException {
    // This is an alias for loadAllExerciseSummaries() for API consistency
    return loadAllExerciseSummaries();
  }

  @Override
  public boolean deleteExercise(UUID exerciseId) throws IOException {
    Objects.requireNonNull(exerciseId, "exerciseId cannot be null");
    Path filePath = getExerciseFilePath(exerciseId);
    return deleteFile(filePath, "Exercise");
  }

  @Override
  public Path getStoragePath() {
    return storageDirectory;
  }

  @Override
  public Optional<UUID> findUnitIdByName(String name) throws IOException {
    List<UnitSummary> summaries = loadUnitSummaries();
    return summaries.stream()
        .filter(summary -> summary.name().equals(name))
        .map(UnitSummary::identifier)
        .findFirst();
  }

  @Override
  public Optional<UUID> findExerciseIdByName(String name) throws IOException {
    List<ExerciseSummary> summaries = loadExerciseSummaries();
    return summaries.stream()
        .filter(summary -> summary.name().equals(name))
        .map(ExerciseSummary::identifier)
        .findFirst();
  }

  private void ensureStorageDirectoryExists(Path dirPath) {
    try {
      if (!Files.isDirectory(dirPath)) {
        log.info("Storage subdirectory does not exist, creating: {}", dirPath);
        Files.createDirectories(dirPath);
      }
    } catch (IOException | SecurityException e) {
      log.error("Failed to create or access storage subdirectory: {}", dirPath, e);
      throw new RuntimeException("Could not initialize storage subdirectory: " + dirPath, e);
    }
  }

  private <T> void saveDtoToFile(T dto, Path filePath) throws IOException {
    try (OutputStream out =
        Files.newOutputStream(
            filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, dto);
    }
  }

  private <S> List<S> loadSummaries(Path directoryPath, Class<S> summaryType, String objectTypeName)
      throws IOException {
    if (!Files.isDirectory(directoryPath)) {
      log.warn(
          "{} storage directory does not exist or is not a directory: {}",
          objectTypeName,
          directoryPath);
      return Collections.emptyList();
    }

    try (Stream<Path> stream = Files.list(directoryPath)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase().endsWith(JSON_FILE_EXTENSION))
          .map(path -> pathToSummary(path, summaryType, objectTypeName))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .sorted(
              Comparator.comparing(
                  summary -> getSummaryName(summary, objectTypeName),
                  String.CASE_INSENSITIVE_ORDER))
          .toList();
    }
  }

  private <S> Optional<S> pathToSummary(
      Path filePath, Class<S> summaryType, String objectTypeName) {
    String filename = filePath.getFileName().toString();
    String identifier = filename.substring(0, filename.length() - JSON_FILE_EXTENSION.length());
    UUID uuid;
    try {
      uuid = UUID.fromString(identifier);
    } catch (IllegalArgumentException e) {
      log.warn("Skipping file with invalid UUID filename for {}: {}", objectTypeName, filename);
      return Optional.empty();
    }

    try (InputStream in = Files.newInputStream(filePath)) {
      JsonNode rootNode = objectMapper.readTree(in);
      JsonNode nameNode = rootNode.path("name");
      if (!nameNode.isMissingNode() && nameNode.isTextual()) {
        String name = nameNode.asText();
        if (summaryType == PlanSummary.class) {
          return Optional.of(summaryType.cast(new PlanSummary(uuid, name)));
        } else if (summaryType == UnitSummary.class) {
          return Optional.of(summaryType.cast(new UnitSummary(uuid, name)));
        } else if (summaryType == ExerciseSummary.class) {
          return Optional.of(summaryType.cast(new ExerciseSummary(uuid, name)));
        } else {
          log.error("Unsupported summary type: {}", summaryType.getName());
          return Optional.empty();
        }
      } else {
        log.warn("Could not find 'name' field in {} file: {}", objectTypeName, filename);
        return Optional.empty();
      }
    } catch (IOException e) {
      log.error("Failed to read or parse summary from {} file: {}", objectTypeName, filename, e);
      return Optional.empty();
    }
  }

  private <S> String getSummaryName(S summary, String objectTypeName) {
    try {
      java.lang.reflect.Method nameMethod = summary.getClass().getMethod("name");
      return (String) nameMethod.invoke(summary);
    } catch (Exception e) {
      log.warn(
          "Could not extract name for sorting summary of type {}: {}", objectTypeName, summary);
      return "";
    }
  }

  private boolean deleteFile(Path filePath, String objectTypeName) throws IOException {
    log.debug("Attempting to delete {} file: {}", objectTypeName, filePath);
    try {
      boolean deleted = Files.deleteIfExists(filePath);
      if (deleted) {
        log.info("Successfully deleted {} file: {}", objectTypeName, filePath.getFileName());
      } else {
        log.warn("{} file to delete not found: {}", objectTypeName, filePath.getFileName());
      }
      return deleted;
    } catch (IOException | SecurityException e) {
      log.error("Failed to delete {} file: {}", objectTypeName, filePath, e);
      throw new IOException(
          "Failed to delete " + objectTypeName + ": " + filePath.getFileName(), e);
    }
  }
}
