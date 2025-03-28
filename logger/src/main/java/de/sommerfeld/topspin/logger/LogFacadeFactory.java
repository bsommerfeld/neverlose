package de.sommerfeld.topspin.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFacadeFactory {

    public static LogFacadeImpl getLogger(Class<?> clazz) {
        Logger slf4jLogger = LoggerFactory.getLogger(clazz);
        return new LogFacadeImpl(slf4jLogger);
    }

    public static LogFacadeImpl getLogger() {
        Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass();
        if (caller == null) {
            throw new IllegalStateException("Unable to determine caller class for logger.");
        }
        return getLogger(caller);
    }
}
