package de.sommerfeld.topspin;

import de.sommerfeld.topspin.bootstrap.Bootstrap;
import de.sommerfeld.topspin.bootstrap.LogDirectorySetup;
import de.sommerfeld.topspin.bootstrap.TopspinBootstrap;

public class Main {

    public static void main(String[] args) {
        LogDirectorySetup.setupLogDirectory();

        Bootstrap topspinBootstrap = new TopspinBootstrap();
        topspinBootstrap.start();
    }
}