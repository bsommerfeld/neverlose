package de.bsommerfeld.neverlose.bootstrap;

import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;

public class NeverloseConfig extends ConfigurablePojo<NeverloseConfig> {

    @Key("first-start")
    private boolean firstStart = true;

    // Stores the SplitPane divider position for the CombinedView (0.0 - 1.0).
    @Key("combined-divider-position")
    private double combinedDividerPosition = 0.3;

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
}
