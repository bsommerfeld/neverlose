package de.bsommerfeld.neverlose.persistence.guice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import de.bsommerfeld.neverlose.persistence.mapper.DefaultPlanMapper;
import de.bsommerfeld.neverlose.persistence.mapper.PlanMapper;
import de.bsommerfeld.neverlose.persistence.service.JsonPlanStorageService;
import de.bsommerfeld.neverlose.persistence.service.PlanStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistenceModule extends AbstractModule {

  private static final LogFacade log = LogFacadeFactory.getLogger();
  private static final String APP_NAME = "NeverLose";

  @Override
  protected void configure() {
    bind(PlanMapper.class).to(DefaultPlanMapper.class).in(Scopes.SINGLETON);
    bind(PlanStorageService.class).to(JsonPlanStorageService.class).in(Scopes.SINGLETON);
  }

  /** Provides a pre-configured, singleton ObjectMapper instance for JSON handling. */
  @Provides
  @Singleton
  ObjectMapper provideObjectMapper() {
    log.debug("Creating ObjectMapper instance");
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }

  /**
   * Provides the platform-specific, absolute path to the storage directory for training plans,
   * ensuring the directory exists. Binds this Path to the name "storage.directory.path".
   */
  @Provides
  @Singleton
  @Named("storage.directory.path")
  Path provideStoragePath() {
    log.debug("Determining storage directory path...");
    String userHome = System.getProperty("user.home");
    if (userHome == null || userHome.isBlank()) {
      log.error(
          "Cannot determine user home directory (user.home property is null or empty). Falling back to relative directory 'data'.");
      Path fallbackPath = Paths.get("data", APP_NAME).toAbsolutePath();
      tryCreateDirectories(fallbackPath);
      return fallbackPath;
    }

    String os = System.getProperty("os.name", "unknown").toLowerCase();
    Path baseDirPath;

    if (os.contains("win")) {
      String localAppDataEnv = System.getenv("LOCALAPPDATA");
      if (localAppDataEnv != null && !localAppDataEnv.isBlank()) {
        baseDirPath = Paths.get(localAppDataEnv);
      } else {
        log.warn(
            "LOCALAPPDATA environment variable not found, using user.home as base for Windows.");
        baseDirPath = Paths.get(userHome);
      }
    } else if (os.contains("mac")) {
      baseDirPath = Paths.get(userHome, "Library", "Application Support");
    } else {
      String dataHomeEnv = System.getenv("XDG_DATA_HOME");
      if (dataHomeEnv != null && !dataHomeEnv.isBlank()) {
        baseDirPath = Paths.get(dataHomeEnv);
      } else {
        baseDirPath = Paths.get(userHome, ".local", "share");
      }
    }

    Path appStoragePath = baseDirPath.resolve(APP_NAME).toAbsolutePath();
    Path finalStoragePath = appStoragePath.resolve("data");
    log.debug("Determined application storage base path: {}", finalStoragePath);
    tryCreateDirectories(finalStoragePath);
    return finalStoragePath;
  }

  private boolean tryCreateDirectories(Path path) {
    try {
      if (Files.isDirectory(path)) {
        return true;
      }
      log.info("Creating storage directory: {}", path);
      Files.createDirectories(path);
      return true;
    } catch (IOException | SecurityException e) {
      log.error("Could not create or access directory: {}", path, e);
      throw new IllegalStateException("Could not create or access directory " + path);
    }
  }
}
