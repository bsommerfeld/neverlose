package de.bsommerfeld.neverlose.fx.theme;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.bsommerfeld.neverlose.logger.LogFacade;
import de.bsommerfeld.neverlose.logger.LogFacadeFactory;
import javafx.application.Platform;
import javafx.scene.Parent;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of TimeBasedThemeService that provides time-based theme switching functionality.
 */
@Singleton
public class TimeBasedThemeServiceImpl implements TimeBasedThemeService {

    private static final LogFacade LOG = LogFacadeFactory.getLogger();
    // Time ranges for different themes
    private static final int MORNING_START_HOUR = 6;
    private static final int AFTERNOON_START_HOUR = 12;
    private static final int EVENING_START_HOUR = 18;
    private Timer themeTimer;

    @Inject
    public TimeBasedThemeServiceImpl() {
        LOG.debug("TimeBasedThemeService initialized");
    }

    @Override
    public void initializeTimeBasedTheme(Parent root) {
        // Apply initial theme based on current time
        applyThemeBasedOnTime(root, LocalTime.now());

        // Schedule theme updates every hour
        themeTimer = new Timer(true);
        themeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> applyThemeBasedOnTime(root, LocalTime.now()));
            }
        }, 0, 60 * 60 * 1000); // Check every hour
    }

    @Override
    public void applyThemeBasedOnTime(Parent root, LocalTime time) {
        int hour = time.getHour();

        // Remove all theme classes first
        root.getStyleClass().removeAll(
                "morning-theme", "afternoon-theme", "evening-theme");

        // Apply appropriate theme based on time
        if (hour >= MORNING_START_HOUR && hour < AFTERNOON_START_HOUR) {
            root.getStyleClass().add("morning-theme");
            LOG.debug("Applied morning theme");
        } else if (hour >= AFTERNOON_START_HOUR && hour < EVENING_START_HOUR) {
            root.getStyleClass().add("afternoon-theme");
            LOG.debug("Applied afternoon theme");
        } else {
            root.getStyleClass().add("evening-theme");
            LOG.debug("Applied evening theme");
        }
    }

    @Override
    public void shutdown() {
        if (themeTimer != null) {
            themeTimer.cancel();
            themeTimer = null;
        }
        LOG.debug("TimeBasedThemeService shutdown complete");
    }
}