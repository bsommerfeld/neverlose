package de.bsommerfeld.neverlose;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.bsommerfeld.neverlose.bootstrap.Bootstrap;
import de.bsommerfeld.neverlose.bootstrap.LogDirectorySetup;
import de.bsommerfeld.neverlose.bootstrap.NeverLoseModule;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();

    private static final String PROPERTIES_FILE = "neverlose.properties";
    private static final String TEST_MODE_FLAG = "-testMode=";

    private static String neverloseVersion = "UNLOADED";
    private static boolean testMode = false;

    private static Injector injector;

    public static void main(String[] args) {
        LogDirectorySetup.setupLogDirectory();
        verifyTestMode(args);
        loadNeverloseVersion();

        injector = Guice.createInjector(new NeverLoseModule());

        Bootstrap topspinBootstrap = injector.getInstance(Bootstrap.class);
        topspinBootstrap.start();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static boolean isTestMode() {
        return testMode;
    }

    public static String getNeverloseVersion() {
        return neverloseVersion;
    }

    private static void loadNeverloseVersion() {
        try (InputStream inputStream =
                     Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            neverloseVersion = properties.getProperty("neverlose.version");
            if (isTestMode()) neverloseVersion = neverloseVersion + "-TEST";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks command line arguments for test mode flag and sets the mode accordingly.
     *
     * @param args Command line arguments to check
     */
    private static void verifyTestMode(String[] args) {
        testMode = hasTestModeFlag(args);
        if (testMode) {
            LOG.debug("Application started in test mode");
        }
    }

    /**
     * Checks if the test mode flag is present in the command line arguments.
     *
     * @param args Command line arguments to check
     *
     * @return true if test mode is enabled, false otherwise
     */
    private static boolean hasTestModeFlag(String[] args) {
        for (String arg : args) {
            if (arg.startsWith(TEST_MODE_FLAG)) {
                return Boolean.parseBoolean(arg.substring(TEST_MODE_FLAG.length()));
            }
        }
        return false;
    }
}
