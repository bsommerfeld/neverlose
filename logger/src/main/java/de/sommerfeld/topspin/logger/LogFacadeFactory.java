package de.sommerfeld.topspin.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogFacadeFactory {

    private LogFacadeFactory() {
    }

    public static LogFacade getLogger(Class<?> clazz) {
        Logger slf4jLogger = LoggerFactory.getLogger(clazz);
        return new LogFacade(slf4jLogger);
    }

    public static LogFacade getLogger(String name) {
        Logger slf4jLogger = LoggerFactory.getLogger(name);
        return new LogFacade(slf4jLogger);
    }

    /**
     * Creates or retrieves a MyLogger for the calling class.
     * Uses StackWalker (Java 9+) for efficient detection.
     * Caution: May be more expensive than getLogger(Class).
     *
     * @return A MyLogger instance for the calling class.
     */
    public static LogFacade getLogger() {
        Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass();
        return getLogger(caller);
    }
}
