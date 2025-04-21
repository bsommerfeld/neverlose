package de.sommerfeld.topspin.persistence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.sommerfeld.topspin.logger.LogFacade;
import de.sommerfeld.topspin.logger.LogFacadeFactory;
import de.sommerfeld.topspin.persistence.dto.TrainingPlanDTO;
import de.sommerfeld.topspin.persistence.mapper.PlanMapper;
import de.sommerfeld.topspin.persistence.model.PlanSummary;
import de.sommerfeld.topspin.plan.TrainingPlan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service implementation for loading and saving TrainingPlan domain objects
 * as JSON files in a designated storage directory.
 */
public class JsonPlanStorageService implements PlanStorageService {

    private static final LogFacade log = LogFacadeFactory.getLogger();
    private static final String JSON_FILE_EXTENSION = ".json";

    private final ObjectMapper objectMapper;
    private final PlanMapper planMapper;
    private final Path storageDirectory;

    @Inject
    public JsonPlanStorageService(
            ObjectMapper objectMapper,
            PlanMapper planMapper,
            @Named("storage.directory.path") Path storageDirectory
    ) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.planMapper = Objects.requireNonNull(planMapper);
        this.storageDirectory = Objects.requireNonNull(storageDirectory);

        ensureStorageDirectoryExists();
        log.info("Initialized JsonPlanStorageService. Storage Directory: {}", this.storageDirectory);
    }

    private void ensureStorageDirectoryExists() {
        try {
            if (!Files.isDirectory(storageDirectory)) {
                log.info("Storage directory does not exist, creating: {}", storageDirectory);
                Files.createDirectories(storageDirectory);
            }
        } catch (IOException | SecurityException e) {
            log.error("Failed to create or access storage directory: {}", storageDirectory, e);
            throw new RuntimeException("Could not initialize storage directory: " + storageDirectory, e);
        }
    }

    @Override
    public String savePlan(TrainingPlan plan) throws IOException {
        Objects.requireNonNull(plan, "TrainingPlan cannot be null");
        String identifier = getPlanIdentifier(plan);
        Path filePath = getFilePath(identifier);
        log.debug("Saving plan '{}' with id {} to file: {}", plan.getName(), plan.getId(), filePath);

        TrainingPlanDTO dto = planMapper.toDTO(plan);
        try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, dto);
            log.info("Successfully saved plan '{}' to {}", plan.getName(), filePath.getFileName());
        } catch (IOException e) {
            log.error("Failed to save plan '{}' to file: {}", plan.getName(), filePath, e);
            throw e;
        }
        return identifier;
    }

    @Override
    public Optional<TrainingPlan> loadPlan(String planIdentifier) throws IOException {
        Objects.requireNonNull(planIdentifier, "planIdentifier cannot be null");
        if (planIdentifier.isBlank()) {
            throw new IllegalArgumentException("planIdentifier cannot be blank");
        }
        Path filePath = getFilePath(planIdentifier);
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
        } catch (IOException e) {
            log.error("Failed to read plan file: {}", filePath, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to map DTO to domain object for plan file: {}", filePath, e);
            throw new IOException("Failed to process plan file: " + filePath.getFileName(), e);
        }
    }

    @Override
    public List<PlanSummary> loadPlanSummaries() throws IOException {
        log.debug("Loading plan summaries from directory: {}", storageDirectory);
        if (!Files.isDirectory(storageDirectory)) {
            log.warn("Storage directory does not exist or is not a directory: {}", storageDirectory);
            return Collections.emptyList();
        }

        try (Stream<Path> stream = Files.list(storageDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(JSON_FILE_EXTENSION))
                    .map(this::pathToPlanSummary)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(Comparator.comparing(PlanSummary::name, String.CASE_INSENSITIVE_ORDER)) // Sort alphabetically by name
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list or process files in storage directory: {}", storageDirectory, e);
            throw e;
        }
    }

    private Optional<PlanSummary> pathToPlanSummary(Path filePath) {
        String identifier = getIdentifierFromPath(filePath);
        if (identifier == null) {
            return Optional.empty();
        }

        try (InputStream in = Files.newInputStream(filePath)) {
            JsonNode rootNode = objectMapper.readTree(in);
            JsonNode nameNode = rootNode.path("name");
            if (!nameNode.isMissingNode() && nameNode.isTextual()) {
                return Optional.of(new PlanSummary(identifier, nameNode.asText()));
            } else {
                log.warn("Could not find 'name' field in plan file: {}", filePath.getFileName());
                return Optional.empty();
            }
        } catch (IOException e) {
            log.error("Failed to read or parse summary from file: {}", filePath.getFileName(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean deletePlan(String planIdentifier) throws IOException {
        Objects.requireNonNull(planIdentifier, "planIdentifier cannot be null");
        if (planIdentifier.isBlank()) {
            throw new IllegalArgumentException("planIdentifier cannot be blank");
        }
        Path filePath = getFilePath(planIdentifier);
        log.debug("Attempting to delete plan file: {}", filePath);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Successfully deleted plan file: {}", filePath.getFileName());
            } else {
                log.warn("Plan file to delete not found: {}", filePath.getFileName());
            }
            return deleted;
        } catch (IOException | SecurityException e) {
            log.error("Failed to delete plan file: {}", filePath, e);
            throw new IOException("Failed to delete plan: " + planIdentifier, e);
        }
    }

    @Override
    public String getPlanIdentifier(TrainingPlan plan) {
        Objects.requireNonNull(plan, "TrainingPlan cannot be null");
        String safeName = plan.getName()
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9_\\-.]+", "_") // Replace non-alphanumeric/-_./ with _
                .replaceAll("_+", "_"); // Collapse multiple underscores
        safeName = safeName.substring(0, Math.min(safeName.length(), 50)); // Limit name part length
        return safeName + "_" + plan.getId().toString();
    }

    @Override
    public Path getStoragePath() {
        return storageDirectory;
    }

    private Path getFilePath(String planIdentifier) {
        return storageDirectory.resolve(planIdentifier + JSON_FILE_EXTENSION);
    }

    private String getIdentifierFromPath(Path filePath) {
        String filename = filePath.getFileName().toString();
        if (filename.toLowerCase().endsWith(JSON_FILE_EXTENSION)) {
            return filename.substring(0, filename.length() - JSON_FILE_EXTENSION.length());
        }
        log.warn("File {} does not have the expected extension {}", filename, JSON_FILE_EXTENSION);
        return null;
    }
}