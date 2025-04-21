package de.sommerfeld.topspin.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to configure the logging directory based on OS conventions.
 */
public class LogDirectorySetup {

    private static final String APP_NAME = "topspin";
    private static final String LOG_FOLDER_NAME = "logs";
    private static final String LOG_DIR_PROPERTY_NAME = "de.sommerfeld.topspin.logdir";
    private static final String FALLBACK_LOG_DIR = "logs";

    private LogDirectorySetup() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Determines the OS-specific base directory for application data.
     * Attempts standard locations for Windows, macOS, and Linux (XDG).
     *
     * @return The Path to the application data base directory. Returns null if user.home is inaccessible.
     */
    private static Path getApplicationDataBaseDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isEmpty()) {
            System.err.println("ERROR: Cannot determine user home directory (user.home property is not set).");
            return null;
        }

        String os = System.getProperty("os.name", "unknown").toLowerCase();
        Path baseDirPath;

        if (os.contains("win")) {
            String localAppDataEnv = System.getenv("LOCALAPPDATA");
            String appDataEnv = System.getenv("APPDATA");
            if (localAppDataEnv != null && !localAppDataEnv.isBlank()) {
                baseDirPath = Paths.get(localAppDataEnv);
            } else if (appDataEnv != null && !appDataEnv.isEmpty()) {
                baseDirPath = Paths.get(appDataEnv);
            } else {
                baseDirPath = Paths.get(userHome, "AppData", "Roaming");
            }
        } else if (os.contains("mac")) {
            baseDirPath = Paths.get(userHome, "Library", "Application Support");
        } else {
            String dataHomeEnv = System.getenv("XDG_DATA_HOME");
            if (dataHomeEnv != null && !dataHomeEnv.isEmpty()) {
                baseDirPath = Paths.get(dataHomeEnv);
            } else {
                baseDirPath = Paths.get(userHome, ".local", "share");
            }
        }
        return baseDirPath;
    }

    /**
     * Tries to create the specified directory path including any necessary parent directories.
     * Prints errors to System.err on failure.
     *
     * @param path The Path to create.
     * @return true if the directory exists or was successfully created, false otherwise.
     */
    private static boolean tryCreateDirectories(Path path) {
        if (path == null) {
            System.err.println("ERROR: Cannot create directories for a null path.");
            return false;
        }
        try {
            if (Files.isDirectory(path)) {
                return true;
            }
            Files.createDirectories(path);
            System.out.println("INFO: Ensured log directory exists: " + path);
            return true;
        } catch (IOException | SecurityException e) {
            System.err.println("ERROR: Could not create or access directory: " + path + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Determines the appropriate log directory based on OS standards,
     * attempts to create it, sets a system property for Logback,
     * and falls back to a local 'logs' directory on failure.
     * Should be called very early during application startup.
     */
    public static void setupLogDirectory() {
        Path baseAppDataDir = getApplicationDataBaseDirectory();
        String effectiveLogDirPath;

        if (baseAppDataDir != null) {
            Path desiredLogPath = baseAppDataDir.resolve(APP_NAME).resolve(LOG_FOLDER_NAME);

            if (tryCreateDirectories(desiredLogPath)) {
                effectiveLogDirPath = desiredLogPath.toString();
                System.out.println("INFO: Using preferred log directory: " + effectiveLogDirPath);
            } else {
                System.err.println("ERROR: Failed to create preferred log directory. Falling back to local directory '" + FALLBACK_LOG_DIR + "'.");
                effectiveLogDirPath = FALLBACK_LOG_DIR;
                tryCreateDirectories(Paths.get(effectiveLogDirPath));
            }
        } else {
            System.err.println("ERROR: Could not determine base application data directory. Falling back to local directory '" + FALLBACK_LOG_DIR + "'.");
            effectiveLogDirPath = FALLBACK_LOG_DIR;
            tryCreateDirectories(Paths.get(effectiveLogDirPath));
        }

        try {
            System.setProperty(LOG_DIR_PROPERTY_NAME, effectiveLogDirPath);
            System.out.println("INFO: System property '" + LOG_DIR_PROPERTY_NAME + "' set to: '" + effectiveLogDirPath + "'");
        } catch (SecurityException e) {
            System.err.println("ERROR: Security restrictions prevent setting system property '" + LOG_DIR_PROPERTY_NAME + "'. Logback might use the default value.");
        }
    }
}
