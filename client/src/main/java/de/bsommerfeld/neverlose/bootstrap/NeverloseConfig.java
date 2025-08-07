package de.bsommerfeld.neverlose.bootstrap;

import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;

public class NeverloseConfig extends ConfigurablePojo<NeverloseConfig> {

    @Key("first-start")
    private boolean firstStart = true;

    public boolean isFirstStart() {
        return firstStart;
    }

    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }
}
