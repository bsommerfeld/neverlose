package de.bsommerfeld.neverlose.bootstrap;

import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;

public class NeverloseConfig extends ConfigurablePojo<NeverloseConfig> {

    @Key("first-start")
    private boolean firstStart = true;

    // Legacy: normalized divider position for the CombinedView (0.0 - 1.0).
    // Used as a fallback when no pixel width is saved.
    @Key("combined-divider-position")
    private double combinedDividerPosition = 0.3;

    // Preferred: persist left pane width in pixels to make size independent of window width.
    // If <= 0, it is considered unset and the legacy normalized position is used.
    @Key("combined-left-width-px")
    private double combinedLeftWidthPx = -1.0;

    public boolean isFirstStart() {
        return firstStart;
    }

    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }

    public double getCombinedDividerPosition() {
        return combinedDividerPosition;
    }

    public void setCombinedDividerPosition(double combinedDividerPosition) {
        if (Double.isNaN(combinedDividerPosition) || Double.isInfinite(combinedDividerPosition)) {
            return;
        }
        if (combinedDividerPosition < 0.0) {
            combinedDividerPosition = 0.0;
        }
        if (combinedDividerPosition > 1.0) {
            combinedDividerPosition = 1.0;
        }
        this.combinedDividerPosition = combinedDividerPosition;
    }

    public double getCombinedLeftWidthPx() {
        return combinedLeftWidthPx;
    }

    public void setCombinedLeftWidthPx(double combinedLeftWidthPx) {
        if (Double.isNaN(combinedLeftWidthPx) || Double.isInfinite(combinedLeftWidthPx)) {
            return;
        }
        if (combinedLeftWidthPx <= 0.0) {
            this.combinedLeftWidthPx = -1.0;
            return;
        }
        this.combinedLeftWidthPx = combinedLeftWidthPx;
    }
}
